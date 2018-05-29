package brightspark.sparkz.energy;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.SparkzConfig;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.util.CommonUtils;
import brightspark.sparkz.util.Pair;
import com.google.common.collect.Iterables;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class EnergyNetwork implements INBTSerializable<NBTTagCompound>
{
    private UUID uuid = UUID.randomUUID();
    private World world;
    private Set<BlockPos> cables = new HashSet<>();
    private Set<BlockPos> consumers = new HashSet<>();
    private Set<BlockPos> producers = new HashSet<>();

    public EnergyNetwork(World world, BlockPos... cables)
    {
        this.world = world;
        Collections.addAll(this.cables, cables);
    }

    public EnergyNetwork(World world, Collection<BlockPos> cables)
    {
        this.world = world;
        this.cables.addAll(cables);
    }

    public EnergyNetwork(World world, NBTTagCompound nbt)
    {
        this.world = world;
        deserializeNBT(nbt);
    }

    /**
     * Get a list of positions of cables in this network that are adjacent to the given position
     */
    private List<BlockPos> findAdjacentCablePositions(BlockPos pos)
    {
        return findAdjacentCablePositions(pos, null);
    }

    private List<BlockPos> findAdjacentCablePositions(BlockPos pos, BlockPos ignorePos)
    {
        List<BlockPos> positions = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos sidePos = pos.offset(facing);
            if(cables.contains(sidePos) && !sidePos.equals(ignorePos))
                positions.add(sidePos);
        }
        return positions;
    }

    /**
     * Merges the networks into this one and then removes the other networks
     */
    @SuppressWarnings("ConstantConditions")
    public void mergeWith(Set<EnergyNetwork> networks)
    {
        Sparkz.logger.info("Merging {} with {} other networks", this, networks.size());
        for(EnergyNetwork otherNetwork : networks)
        {
            otherNetwork.cables.forEach((pos) -> ((TileCable) world.getTileEntity(pos)).setNetwork(this));
            cables.addAll(otherNetwork.cables);
            consumers.addAll(otherNetwork.consumers);
            producers.addAll(otherNetwork.producers);
            NetworkData.removeNetwork(world, otherNetwork);
        }
    }

    /**
     * Splits this network if the removed position causes a gap in this network
     * Returns the new networks as a result of the split
     * TODO: Consumers and producers!
     */
    public void splitAt(BlockPos pos)
    {
        Set<Set<BlockPos>> cableNetworks = CommonUtils.getAllAdjacentConnectedCables(world, pos);

        int size = cableNetworks.size();
        if(size == 1)
            //No need to create new networks
            cables = Iterables.getOnlyElement(cableNetworks);
        else
        {
            //We don't need to replace one of the networks - we can re-use this one
            Iterator<Set<BlockPos>> iter = cableNetworks.iterator();
            Set<BlockPos> first = iter.next();
            iter.remove();

            //Remove all cables from this network that aren't in the removed set
            cables.removeIf(cable -> !first.contains(cable));

            //Create new networks
            Sparkz.logger.info("Adding {} new networks due to split of network {}", cableNetworks.size(), this);
            cableNetworks.forEach(c -> NetworkData.addNewNetwork(world, c));
        }
    }

    public void update()
    {
        //On EnergyNetwork update - transfer power from producers to consumers
        //TODO: Treat blocks which are both a consumer and producer as excess storage?
        //TODO: Need to review distribution logic

        if(SparkzConfig.mergeEnergy)
            transferEnergyMerged();
        else
            transferEnergySeparate();
    }

    /**
     * Merge energy - Convert energy from producers to FE, then convert to what consumers need
     */
    private void transferEnergyMerged()
    {
        //Get producers
        List<EnergyBlock> producerEnergy = new LinkedList<>();
        double totalProducerEnergy = 0D;
        for(BlockPos pos : producers)
        {
            TileEntity te = world.getTileEntity(pos);
            if(te != null)
            {
                IEnergy energy = IEnergy.create(te, null);
                if(energy != null)
                {
                    EnumFacing sideWithCable = CommonUtils.getSideWithCable(world, pos, this);
                    if(sideWithCable != null)
                    {
                        producerEnergy.add(new EnergyBlock(energy, pos, sideWithCable));
                        totalProducerEnergy += energy.getMaxOutputType(sideWithCable, EnergyType.FORGE_ENERGY);
                    }
                }
            }
        }
        //If nothing to output to, then just return
        if(producerEnergy.isEmpty()) return;
        //Sort producers by max output
        producerEnergy.sort(Comparator.comparingLong(e -> e.energy.getMaxOutput(e.side)));

        //Get consumers
        List<EnergyBlock> consumerEnergy = new LinkedList<>();
        for(BlockPos pos : consumers)
        {
            if(producers.contains(pos)) continue;
            TileEntity te = world.getTileEntity(pos);
            if(te != null)
            {
                IEnergy energy = IEnergy.create(te, null);
                if(energy != null)
                {
                    EnumFacing sideWithCable = CommonUtils.getSideWithCable(world, pos, this);
                    if(sideWithCable != null)
                        consumerEnergy.add(new EnergyBlock(energy, pos, sideWithCable));
                }
            }
        }
        //Sort consumers by max input
        consumerEnergy.sort(Comparator.comparingLong(e -> e.energy.getMaxInput(e.side)));

        //Transfer energy
        distributeEnergy(producerEnergy, totalProducerEnergy, consumerEnergy);
    }

    /**
     * Don't merge energy - Only send energy from producers to consumers of the same energy type
     */
    private void transferEnergySeparate()
    {
        //Get producers
        Map<EnergyType, Pair<InternalEnergy, List<EnergyBlock>>> producerEnergy = new HashMap<>();
        for(BlockPos pos : producers)
        {
            TileEntity te = world.getTileEntity(pos);
            if(te != null)
            {
                IEnergy energy = IEnergy.create(te, null);
                if(energy != null)
                {
                    EnumFacing sideWithCable = CommonUtils.getSideWithCable(world, pos, this);
                    if(sideWithCable != null)
                    {
                        Pair<InternalEnergy, List<EnergyBlock>> type = producerEnergy.computeIfAbsent(energy.getEnergyType(),
                                energyType -> new Pair<>(new InternalEnergy(), new LinkedList<>()));
                        type.key.addMaxOutput(energy, sideWithCable);
                        type.value.add(new EnergyBlock(energy, pos, sideWithCable));
                    }
                }
            }
        }
        //If nothing to output to, then just return
        if(producerEnergy.isEmpty()) return;
        //Sort producers by max output
        producerEnergy.values().forEach(type -> type.value.sort(Comparator.comparingLong(e -> e.energy.getMaxOutput(e.side))));

        //Get consumers
        Map<EnergyType, List<EnergyBlock>> consumerEnergy = new HashMap<>();
        for(BlockPos pos : consumers)
        {
            if(producers.contains(pos)) continue;
            TileEntity te = world.getTileEntity(pos);
            if(te != null)
            {
                IEnergy energy = IEnergy.create(te, null);
                if(energy != null)
                {
                    EnumFacing sideWithCable = CommonUtils.getSideWithCable(world, pos, this);
                    if(sideWithCable != null)
                    {
                        List<EnergyBlock> typeConsumers = consumerEnergy.computeIfAbsent(energy.getEnergyType(), k -> new LinkedList<>());
                        typeConsumers.add(new EnergyBlock(energy, pos, sideWithCable));
                    }
                }
            }
        }

        //Transfer energy per type
        for(Map.Entry<EnergyType, List<EnergyBlock>> consumersByType : consumerEnergy.entrySet())
        {
            EnergyType type = consumersByType.getKey();
            List<EnergyBlock> typeConsumers = consumersByType.getValue();
            Pair<InternalEnergy, List<EnergyBlock>> producersPair = producerEnergy.get(type);

            //Sort consumers by max input
            typeConsumers.sort(Comparator.comparingLong(e -> e.energy.getMaxInput(e.side)));
            //Distribute energy
            distributeEnergy(producersPair.value, producersPair.key.convertTo(type), typeConsumers);
        }
    }

    /**
     * Distributes energy from producers to consumers of the same energy type
     */
    private static void distributeEnergy(List<EnergyBlock> producersEnergy, long producersTotal, List<EnergyBlock> consumersEnergy)
    {
        long producerEnergyLeft = producersTotal;

        //Evenly distribute energy to consumers
        for(EnergyBlock consumer : consumersEnergy)
        {
            long provided = producerEnergyLeft / consumersEnergy.size();
            long actuallyAccepted = consumer.energy.inputEnergy(consumer.side, provided);
            producerEnergyLeft -= actuallyAccepted;
        }

        //Get the amount actually provided to consumers
        long energyUsed = producersTotal - producerEnergyLeft;
        int producersLeftToExtractFrom = producersEnergy.size();

        //Evenly draw energy from producers
        for(EnergyBlock producer : producersEnergy)
        {
            long taking = energyUsed / producersLeftToExtractFrom;
            long actuallyTaken = producer.energy.outputEnergy(producer.side, taking);
            energyUsed -= actuallyTaken;
        }
    }

    /**
     * Distributes energy from producers to consumers of any energy type
     */
    private static void distributeEnergy(List<EnergyBlock> producersEnergy, double producersTotal, List<EnergyBlock> consumersEnergy)
    {
        double producerEnergyLeft = producersTotal;

        //Evenly distribute energy to consumers
        for(EnergyBlock consumer : consumersEnergy)
        {
            long provided = (long) (producerEnergyLeft / (double) consumersEnergy.size());
            long actuallyAccepted = consumer.energy.inputEnergy(consumer.side,
                    (long) EnergyType.INTERNAL_ENERGY.convertTo(provided, consumer.energy.getEnergyType()));
            producerEnergyLeft -= actuallyAccepted;
        }

        //Get the amount actually provided to consumers
        double energyUsed = producersTotal - producerEnergyLeft;
        int producersLeftToExtractFrom = producersEnergy.size();

        //Evenly draw energy from producers
        for(EnergyBlock producer : producersEnergy)
        {
            long taking = (long) (energyUsed / (double) producersLeftToExtractFrom);
            long actuallyTaken = producer.energy.outputEnergy(producer.side, taking);
            energyUsed -= actuallyTaken;
        }
    }

    /**
     * Checks if the given position is a cable in the network or is adjacent to a cable in this network
     */
    private boolean isComponentInNetwork(BlockPos pos)
    {
        if(cables.contains(pos))
            return true;
        for(BlockPos cablePos : cables)
            if(CommonUtils.areBlocksAdjacent(cablePos, pos))
                return true;
        return false;
    }

    /**
     * Checks every block in the network and returns whether it has changed
     */
    public boolean checkNetwork()
    {
        boolean changed = false;

        Iterator<BlockPos> iterator = cables.iterator();
        while(iterator.hasNext())
        {
            BlockPos pos = iterator.next();
            if(!CommonUtils.isCable(world, pos) || !isComponentInNetwork(pos))
            {
                //Split up networks if break in this network
                NetworkData.onCableRemoved(world, pos);
                changed = true;
                iterator.remove();
            }
            else
            {
                //Check for consumers/producers around cable
                for(EnumFacing side : EnumFacing.VALUES)
                {
                    BlockPos sidePos = pos.offset(side);
                    EnumFacing opposite = side.getOpposite();
                    IEnergy energy = IEnergy.create(world, sidePos, opposite);
                    if(energy == null) continue;
                    if(energy.canInput(opposite))   addConsumer(sidePos);
                    if(energy.canOutput(opposite))  addProducer(sidePos);
                }
            }
        }

        iterator = consumers.iterator();
        while(iterator.hasNext())
        {
            BlockPos pos = iterator.next();
            IEnergy energy = IEnergy.create(world, pos, null);
            EnumFacing sideWithCable = CommonUtils.getSideWithCable(world, pos, this);
            if(energy == null || sideWithCable == null || !isComponentInNetwork(pos) || !energy.canInput(sideWithCable))
            {
                changed = true;
                iterator.remove();
            }
            else if(energy.canOutput(sideWithCable))
            {
                changed = true;
                addProducer(pos);
            }
        }

        iterator = producers.iterator();
        while(iterator.hasNext())
        {
            BlockPos pos = iterator.next();
            IEnergy energy = IEnergy.create(world, pos, null);
            EnumFacing sideWithCable = CommonUtils.getSideWithCable(world, pos, this);
            if(energy == null || sideWithCable == null || !isComponentInNetwork(pos) || !energy.canOutput(sideWithCable))
            {
                changed = true;
                iterator.remove();
            }
            else if(energy.canInput(sideWithCable))
            {
                changed = true;
                addConsumer(pos);
            }
        }

        return changed;
    }

    public boolean hasCables()
    {
        return !cables.isEmpty();
    }

    /**
     * Check if the given cable position is a part of this network
     */
    public boolean hasCablePos(BlockPos cablePos)
    {
        for(BlockPos pos : cables)
            if(pos.equals(cablePos))
                return true;
        return false;
    }

    /**
     * Checks if the given position is adjacent to a cable in this network
     */
    public boolean canAddComponent(BlockPos componentPos)
    {
        for(BlockPos pos : cables)
            if(CommonUtils.areBlocksAdjacent(pos, componentPos))
                return true;
        return false;
    }

    public boolean removeIO(BlockPos pos)
    {
        return consumers.remove(pos) | producers.remove(pos);
    }

    public Set<BlockPos> getCables()
    {
        return cables;
    }

    public int getNumCables()
    {
        return cables.size();
    }

    public void addCable(BlockPos pos)
    {
        cables.add(pos);
    }

    public void removeCable(BlockPos pos)
    {
        cables.remove(pos);
    }

    public Set<BlockPos> getConsumers()
    {
        return consumers;
    }

    public int getNumConsumers()
    {
        return consumers.size();
    }

    public void addConsumer(BlockPos pos)
    {
        Sparkz.logger.info("Adding {} as consumer to network {}", pos, this);
        consumers.add(pos);
    }

    public void removeInput(BlockPos pos)
    {
        consumers.remove(pos);
    }

    public Set<BlockPos> getProducers()
    {
        return producers;
    }

    public int getNumProducers()
    {
        return producers.size();
    }

    public void addProducer(BlockPos pos)
    {
        Sparkz.logger.info("Adding {} as producer to network {}", pos, this);
        producers.add(pos);
    }

    public void removeOutput(BlockPos pos)
    {
        producers.remove(pos);
    }

    public UUID getUuid()
    {
        return uuid;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof EnergyNetwork && uuid.equals(((EnergyNetwork) obj).uuid);
    }

    @Override
    public String toString()
    {
        return uuid.toString();
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setUniqueId("uuid", uuid);
        nbt.setInteger("dimension", world.provider.getDimension());

        NBTTagList list = new NBTTagList();
        for(BlockPos pos : cables)
            list.appendTag(new NBTTagLong(pos.toLong()));
        nbt.setTag("cableList", list);

        list = new NBTTagList();
        for(BlockPos pos : consumers)
            list.appendTag(new NBTTagLong(pos.toLong()));
        nbt.setTag("consumerList", list);

        list = new NBTTagList();
        for(BlockPos pos : producers)
            list.appendTag(new NBTTagLong(pos.toLong()));
        nbt.setTag("producerList", list);

        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        uuid = nbt.getUniqueId("uuid");
        world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(nbt.getInteger("dimension"));

        cables.clear();
        NBTTagList list = nbt.getTagList("cableList", Constants.NBT.TAG_LONG);
        list.forEach(tag -> cables.add(BlockPos.fromLong(((NBTTagLong) tag).getLong())));

        consumers.clear();
        list = nbt.getTagList("consumerList", Constants.NBT.TAG_LONG);
        list.forEach(tag -> consumers.add(BlockPos.fromLong(((NBTTagLong) tag).getLong())));

        producers.clear();
        list = nbt.getTagList("producerList", Constants.NBT.TAG_LONG);
        list.forEach(tag -> producers.add(BlockPos.fromLong(((NBTTagLong) tag).getLong())));
    }
}
