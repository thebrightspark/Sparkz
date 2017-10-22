package brightspark.sparkz.energy;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.util.CommonUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

@Mod.EventBusSubscriber
public class NetworkHandler
{
    //TODO: Need to think about how this will all work with dimensions!

    private static List<EnergyNetwork> networks = new ArrayList<>();
    private static ScheduledExecutorService threadScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, Sparkz.MOD_NAME + "EnergyHandler Thread"));

    private static void runInThread(Runnable runnable)
    {
        threadScheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    private static EnergyNetwork getNetworkWithCable(BlockPos cablePos)
    {
        for(EnergyNetwork network : networks)
            if(network.hasCablePos(cablePos))
                return network;
        return null;
    }

    /**
     * Tries to set the network to the cable if it's a cable
     * Returns if the block was a cable
     */
    private static boolean trySetNetworkToCable(IBlockAccess world, BlockPos cablePos, EnergyNetwork network)
    {
        TileEntity te = world.getTileEntity(cablePos);
        if(te instanceof TileCable)
        {
            ((TileCable) te).setNetwork(network);
            return true;
        }
        return false;
    }

    /**
     * Adds the component to the network
     * Returns is successful
     */
    private static boolean addToEnergyNetwork(EnergyNetwork network, World world, BlockPos componentPos)
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
                network.addInput(componentPos);
            else if(energy.canInput())
                network.addOutput(componentPos);
        }
        return true;
    }

    /**
     * Tries to add the component to an adjacent network
     * If none found, will create a new network for it
     */
    public static void addNewComponent(World world, BlockPos componentPos)
    {
        if(world.isRemote) return;
        runInThread(() ->
        {
            //Add to existing adjacent network if there is one
            List<EnergyNetwork> adjacentNetworks = CommonUtils.findAdjacentNetworks(world, componentPos);
            if(adjacentNetworks.size() > 0)
            {
                EnergyNetwork network1 = adjacentNetworks.remove(0);
                if(addToEnergyNetwork(network1, world, componentPos))
                {
                    Sparkz.logger.info("Added block {} at {} to energy network {}",
                            world.getBlockState(componentPos).getBlock().getRegistryName(), componentPos, network1);
                    if(adjacentNetworks.size() > 0)
                        //Merge other network to this one
                        network1.mergeWith(world, adjacentNetworks);
                    return;
                }
            }
            //No network found adjacent to block placed - Create new network
            EnergyNetwork network = newEnergyNetwork(world, componentPos);
            Sparkz.logger.info("Added block {} at {} to NEW energy network {}",
                    world.getBlockState(componentPos).getBlock().getRegistryName(), componentPos, network);
        });
    }

    public static void removeNetwork(EnergyNetwork network)
    {
        Sparkz.logger.info("Removing network {}", network);
        networks.remove(network);
    }

    public static EnergyNetwork newEnergyNetwork(World world, BlockPos cable)
    {
        if(world.isRemote) return null;
        EnergyNetwork network = new EnergyNetwork(cable);
        if(!trySetNetworkToCable(world, cable, network))
        {
            Sparkz.logger.info("Cannot create new network, as block at {} is not a cable", cable);
            return null;
        }
        networks.add(network);
        Sparkz.logger.info("Created new network {}", network);
        return network;
    }

    public static void onCableRemoved(World world, BlockPos cablePos)
    {
        if(world.isRemote) return;
        onCableRemoved(world, cablePos, getNetworkWithCable(cablePos));
    }

    public static void onCableRemoved(World world, BlockPos cablePos, EnergyNetwork network)
    {
        if(world.isRemote || network == null) return;
        Sparkz.logger.info("Removed cable at {} from network {}", cablePos, network);
        network.removeCable(cablePos);
        if(network.hasCables())
        {
            if(CommonUtils.countAdjacentCables(world, cablePos) > 1)
            {
                Sparkz.logger.info("Splitting network {} at position {}", network, cablePos);
                runInThread(() -> {
                    List<EnergyNetwork> newNetworks = network.splitAt(world, cablePos);
                    if(newNetworks.size() > 0)
                    {
                        Sparkz.logger.info("Adding {} new networks due to split of network {}",
                                newNetworks.size(), network);
                        //Set new networks to cables
                        for(EnergyNetwork newNetwork : newNetworks)
                        {
                            for(BlockPos pos : newNetwork.getCables())
                            {
                                TileEntity te = world.getTileEntity(pos);
                                if(te instanceof TileCable)
                                    ((TileCable) te).setNetwork(newNetwork);
                                else
                                    newNetwork.removeCable(pos);
                            }
                        }
                        networks.addAll(newNetworks);
                    }
                });
            }
        }
        else
            removeNetwork(network);
    }

    @SubscribeEvent
    public static void tickNetworks(TickEvent.WorldTickEvent event)
    {
        if(event.side != Side.SERVER || event.phase != TickEvent.Phase.START)
            return;

        //Tick networks
        World world = event.world;
        Iterator<EnergyNetwork> iterator = networks.iterator();
        while(iterator.hasNext())
        {
            EnergyNetwork network = iterator.next();
            if(network.hasCables())
                network.update(world);
            else
                iterator.remove();
        }
    }
}
