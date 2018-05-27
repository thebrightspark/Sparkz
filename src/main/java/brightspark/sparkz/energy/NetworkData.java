package brightspark.sparkz.energy;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.util.CommonUtils;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod.EventBusSubscriber
public class NetworkData extends WorldSavedData
{
    private static final String NAME = Sparkz.MOD_ID + "_networks";
    private static ExecutorService executorService = Executors.newCachedThreadPool(
            r -> new Thread(r, Sparkz.MOD_NAME + "EnergyHandler Thread"));

    private final Set<EnergyNetwork> networks = new HashSet<>();
    private World world;
    private int dimension;

    public NetworkData(World world)
    {
        super(NAME);
        this.world = world;
    }

    public NetworkData(String name)
    {
        super(name);
    }

    @Override
    public boolean isDirty()
    {
        return true;
    }

    private void ensureWorld()
    {
        if(world == null) world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
    }

    public static NetworkData get(World world)
    {
        MapStorage storage = world.getPerWorldStorage();
        NetworkData data = (NetworkData) storage.getOrLoadData(NetworkData.class, NAME);
        if(data == null)
        {
            data = new NetworkData(world);
            storage.setData(NAME, data);
        }
        return data;
    }

    private static void runInThread(Runnable runnable)
    {
        executorService.execute(runnable);
    }

    private EnergyNetwork getNetworkWithCableInternal(BlockPos cablePos)
    {
        for(EnergyNetwork network : networks)
            if(network.hasCablePos(cablePos))
                return network;
        return null;
    }

    public static EnergyNetwork getNetworkWithCable(World world, BlockPos cablePos)
    {
        NetworkData data = get(world);
        return data == null ? null : data.getNetworkWithCableInternal(cablePos);
    }

    /**
     * Tries to set the network to the cable if it's a cable
     * Returns if the block was a cable
     */
    private boolean trySetNetworkToCable(IBlockAccess world, BlockPos cablePos, EnergyNetwork network)
    {
        TileEntity te = world.getTileEntity(cablePos);
        if(te instanceof TileCable)
        {
            ((TileCable) te).setNetwork(network);

            //Check for IO blocks around and add them to the network
            for(EnumFacing side : EnumFacing.VALUES)
            {
                BlockPos neighbour = cablePos.offset(side);
                IEnergy energy = IEnergy.create(world, neighbour, null);
                if(energy != null)
                {
                    if(energy.canInput())   network.addConsumer(neighbour);
                    if(energy.canOutput())  network.addProducer(neighbour);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Adds the component to the network
     * Returns is successful
     */
    private boolean addToEnergyNetwork(EnergyNetwork network, World world, BlockPos componentPos)
    {
        TileEntity te = world.getTileEntity(componentPos);
        if(te == null) return false;
        if(trySetNetworkToCable(world, componentPos, network))
            network.addCable(componentPos);
        else
        {
            IEnergy energy = IEnergy.create(te, null);
            if(energy == null)
                return false;
            if(energy.canOutput())
                network.addConsumer(componentPos);
            else if(energy.canInput())
                network.addProducer(componentPos);
        }
        return true;
    }

    /**
     * Tries to add the component to an adjacent network
     * If none found, will create a new network for it
     */
    private void addNewComponentInternal(World world, BlockPos componentPos)
    {
        runInThread(() ->
        {
            //Add to existing adjacent network if there is one
            Set<EnergyNetwork> adjacentNetworks = CommonUtils.findAdjacentNetworks(world, componentPos);
            Iterator<EnergyNetwork> adjacentIter = adjacentNetworks.iterator();

            if(adjacentIter.hasNext())
            {
                EnergyNetwork network1 = adjacentIter.next();
                if(addToEnergyNetwork(network1, world, componentPos))
                {
                    Sparkz.logger.info("Added block {} at {} to energy network {}", world.getBlockState(componentPos).getBlock().getRegistryName(), componentPos, network1);
                    if(adjacentIter.hasNext())
                    {
                        adjacentNetworks.remove(network1);
                        network1.mergeWith(adjacentNetworks);
                    }
                    return;
                }
            }
            //No network found adjacent to block placed - Create new network
            EnergyNetwork network = addNewNetworkInternal(world, componentPos);
            Sparkz.logger.info("Added block {} at {} to NEW energy network {}",
                    world.getBlockState(componentPos).getBlock().getRegistryName(), componentPos, network);
        });
    }

    public static void addNewComponent(World world, BlockPos componentPos)
    {
        NetworkData data = get(world);
        if(data != null) data.addNewComponentInternal(world, componentPos);
    }

    private void removeNetworkInternal(EnergyNetwork network)
    {
        Sparkz.logger.info("Removing network {}", network);
        networks.remove(network);
    }

    public static void removeNetwork(World world, EnergyNetwork network)
    {
        NetworkData data = get(world);
        if(data != null) data.removeNetworkInternal(network);
    }

    private EnergyNetwork addNewNetworkInternal(World world, BlockPos... cables)
    {
        return addNewNetworkInternal(world, Sets.newHashSet(cables));
    }

    private EnergyNetwork addNewNetworkInternal(World world, Set<BlockPos> cables)
    {
        if(world.isRemote) return null;
        EnergyNetwork network = new EnergyNetwork(world, cables);
        for(BlockPos cable : cables)
        {
            if(!trySetNetworkToCable(world, cable, network))
            {
                Sparkz.logger.info("Cannot add network to block at {} as it is not a cable", cable);
                network.removeCable(cable);
            }
        }
        networks.add(network);
        Sparkz.logger.info("Created new network {} with {} cables", network, network.getCables().size());
        return network;
    }

    public static EnergyNetwork addNewNetwork(World world, Set<BlockPos> cables)
    {
        NetworkData data = get(world);
        return data == null ? null : data.addNewNetworkInternal(world, cables);
    }

    private EnergyNetwork getNetworkInternal(UUID networkUuid)
    {
        for(EnergyNetwork network : networks)
            if(network.getUuid().equals(networkUuid))
                return network;
        return null;
    }

    public static EnergyNetwork getNetwork(World world, UUID networkUuid)
    {
        NetworkData data = get(world);
        return data == null ? null : data.getNetworkInternal(networkUuid);
    }

    private void onCableRemovedInternal(World world, BlockPos cablePos)
    {
        onCableRemovedInternal(world, cablePos, getNetworkWithCableInternal(cablePos));
    }

    public static void onCableRemoved(World world, BlockPos cablePos)
    {
        NetworkData data = get(world);
        if(data != null) data.onCableRemovedInternal(world, cablePos);
    }

    private void onCableRemovedInternal(World world, BlockPos cablePos, EnergyNetwork network)
    {
        if(world.isRemote || network == null) return;
        Sparkz.logger.info("Removed cable at {} from network {}", cablePos, network);
        network.removeCable(cablePos);
        if(network.hasCables())
        {
            if(CommonUtils.countAdjacentCables(world, cablePos) > 1)
            {
                Sparkz.logger.info("Splitting network {} at position {}", network, cablePos);
                runInThread(() -> network.splitAt(cablePos));
            }
        }
        else
            removeNetworkInternal(network);
    }

    public static void tickNetworks(World world)
    {
        NetworkData data = get(world);
        if(data == null) return;
        Iterator<EnergyNetwork> iterator = data.networks.iterator();
        while(iterator.hasNext())
        {
            EnergyNetwork network = iterator.next();
            if(network.hasCables())
                network.update();
            else
                iterator.remove();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        dimension = nbt.getInteger("dimension");

        NBTTagList list = nbt.getTagList("networks", Constants.NBT.TAG_COMPOUND);
        list.forEach(tag -> networks.add(new EnergyNetwork(world, (NBTTagCompound) tag)));
        Sparkz.logger.info(">>>>>>>>>>>>>> Loaded {} networks", networks.size());

        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        //Get the world
        if(world == null)
        {
            //Set networks in all loaded cables
            ensureWorld();
            for(EnergyNetwork network : networks)
            {
                for(BlockPos cablePos : network.getCables())
                {
                    if(world.isBlockLoaded(cablePos))
                    {
                        TileEntity te = world.getTileEntity(cablePos);
                        if(te instanceof TileCable)
                        {
                            ((TileCable) te).setNetwork(network);
                            Sparkz.logger.info("============= Set network {} for cable at {}", network, cablePos);
                        }
                    }
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("dimension", dimension);

        NBTTagList list = new NBTTagList();
        for(EnergyNetwork network : networks)
        {
            network.checkNetwork();
            if(!network.hasCables())
                removeNetworkInternal(network);
            else
                list.appendTag(network.serializeNBT());
        }
        nbt.setTag("networks", list);
        Sparkz.logger.info(">>>>>>>>>>>>>> Saved {} networks", networks.size());

        ensureWorld();
        nbt.setInteger("dimension", world.provider.getDimension());

        return nbt;
    }
}
