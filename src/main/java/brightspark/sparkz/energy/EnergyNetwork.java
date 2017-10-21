package brightspark.sparkz.energy;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.util.CommonUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EnergyNetwork
{
    private List<BlockPos> cables = new ArrayList<>();
    private List<BlockPos> inputs = new ArrayList<>();
    private List<BlockPos> outputs = new ArrayList<>();

    public EnergyNetwork(BlockPos... cables)
    {
        Collections.addAll(this.cables, cables);
    }

    public EnergyNetwork(List<BlockPos> cables)
    {
        this.cables.addAll(cables);
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
            if(cables.contains(sidePos) && (ignorePos == null || !sidePos.equals(ignorePos)))
                positions.add(sidePos);
        }
        return positions;
    }

    /**
     * Merges the networks into this one and then removes the other networks
     */
    public void mergeWith(IBlockAccess world, List<EnergyNetwork> networks)
    {
        mergeWith(world, networks.toArray(new EnergyNetwork[networks.size()]));
    }

    /**
     * Merges the networks into this one and then removes the other networks
     */
    @SuppressWarnings("ConstantConditions")
    public void mergeWith(IBlockAccess world, EnergyNetwork... networks)
    {
        Sparkz.logger.info("Merging {} with {} other networks", this, networks.length);
        for(EnergyNetwork otherNetwork : networks)
        {
            otherNetwork.cables.forEach((pos) -> ((TileCable) world.getTileEntity(pos)).setNetwork(this));
            cables.addAll(otherNetwork.cables);
            inputs.addAll(otherNetwork.inputs);
            outputs.addAll(otherNetwork.outputs);
            EnergyHandler.removeNetwork(otherNetwork);
        }
    }

    /**
     * Splits this network if the removed position causes a gap in this network
     * Returns the new networks as a result of the split
     */
    public List<EnergyNetwork> splitAt(World world, BlockPos pos)
    {
        List<EnergyNetwork> newNetworks = new ArrayList<>(1);

        List<List<BlockPos>> cableNetworks = CommonUtils.getAllAdjacentConnectedCables(world, pos);
        if(cableNetworks.size() > 0)
            cables = cableNetworks.get(0);
        if(cableNetworks.size() > 1)
            for(int i = 1; i < cableNetworks.size(); i++)
                newNetworks.add(new EnergyNetwork(cableNetworks.get(i)));

        //Check the network for any components which are no longer part of the network
        checkNetwork(world);

        return newNetworks;
    }

    public void update(World world)
    {
        //TODO: On EnergyNetwork update - transfer power from inputs to outputs
        //Check how much is requested from outputs, and then distribute inputs to them evenly

        int totalOutputRequested = 0;

        //Get energy storage for outputs
        List<IEnergy> outputEnergy = new ArrayList<>();
        for(BlockPos pos : outputs)
        {
            TileEntity te = world.getTileEntity(pos);
            if(te != null)
            {
                IEnergy energy = IEnergy.create(te, null);
                if(energy != null)
                {
                    outputEnergy.add(energy);
                    totalOutputRequested += energy.getMaxInput();
                }
            }
        }

        //TODO: Collect inputs and distribute to outputs evenly
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
    public boolean checkNetwork(World world)
    {
        boolean changed = false;

        Iterator<BlockPos> iterator = cables.iterator();
        while(iterator.hasNext())
        {
            BlockPos pos = iterator.next();
            if(!CommonUtils.isCable(world, pos) || !isComponentInNetwork(pos))
            {
                //Split up networks if break in this network
                EnergyHandler.onCableRemoved(world, pos);
                changed = true;
                iterator.remove();
            }
        }

        iterator = inputs.iterator();
        while(iterator.hasNext())
        {
            BlockPos pos = iterator.next();
            IEnergy energy = IEnergy.create(world, pos, null);
            if(energy == null || !energy.canInput() || !isComponentInNetwork(pos))
            {
                changed = true;
                iterator.remove();
            }
        }

        iterator = outputs.iterator();
        while(iterator.hasNext())
        {
            BlockPos pos = iterator.next();
            IEnergy energy = IEnergy.create(world, pos, null);
            if(energy == null || !energy.canOutput() || !isComponentInNetwork(pos))
            {
                changed = true;
                iterator.remove();
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

    public List<BlockPos> getCables()
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

    public int getNumInputs()
    {
        return inputs.size();
    }

    public void addInput(BlockPos pos)
    {
        inputs.add(pos);
    }

    public void removeInput(BlockPos pos)
    {
        inputs.remove(pos);
    }

    public int getNumOutputs()
    {
        return outputs.size();
    }

    public void addOutput(BlockPos pos)
    {
        outputs.add(pos);
    }

    public void removeOutput(BlockPos pos)
    {
        outputs.remove(pos);
    }

    @Override
    public String toString()
    {
        return Integer.toHexString(hashCode());
    }
}
