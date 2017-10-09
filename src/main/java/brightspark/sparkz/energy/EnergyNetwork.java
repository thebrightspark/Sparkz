package brightspark.sparkz.energy;

import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.util.CommonUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EnergyNetwork
{
    private List<BlockPos> cables = new ArrayList<>();
    private List<BlockPos> inputs = new ArrayList<>();
    private List<BlockPos> outputs = new ArrayList<>();

    public EnergyNetwork(World world, BlockPos cable)
    {
        cables.add(cable);
    }

    /**
     * Merges the network into this one and then removes the other network
     */
    public void mergeWith(EnergyNetwork network)
    {
        cables.addAll(network.cables);
        inputs.addAll(network.inputs);
        outputs.addAll(network.outputs);
        EnergyHandler.removeNetwork(network);
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
     * Checks every block in the network and returns whether it has changed
     */
    public boolean checkNetwork(IBlockAccess world)
    {
        boolean changed = false;

        Iterator<BlockPos> iterator = cables.iterator();
        while(iterator.hasNext())
        {
            if(!isCableBlock(world, iterator.next()))
            {
                changed = true;
                iterator.remove();
                //TODO: Need to split up networks if break in this network
            }
        }

        iterator = inputs.iterator();
        while(iterator.hasNext())
        {
            IEnergy energy = IEnergy.create(world, iterator.next(), null);
            if(energy == null || !energy.canInput())
            {
                changed = true;
                iterator.remove();
            }
        }

        iterator = outputs.iterator();
        while(iterator.hasNext())
        {
            IEnergy energy = IEnergy.create(world, iterator.next(), null);
            if(energy == null || !energy.canOutput())
            {
                changed = true;
                iterator.remove();
            }
        }

        return changed;
    }

    private boolean isCableBlock(IBlockAccess world, BlockPos pos)
    {
        return world.getBlockState(pos).getBlock() instanceof BlockCable;
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

    public void addCable(IBlockAccess world, BlockPos pos)
    {
        cables.add(pos);

        //Merge two newly joined networks
        EnergyNetwork otherNetwork = CommonUtils.findAdjacentNetwork(world, pos, this);
        if(otherNetwork != null)
            mergeWith(otherNetwork);
    }

    public void removeCable(BlockPos pos)
    {
        cables.remove(pos);
    }

    public void addInput(BlockPos pos)
    {
        inputs.add(pos);
    }

    public void removeInput(BlockPos pos)
    {
        inputs.remove(pos);
    }

    public void addOutput(BlockPos pos)
    {
        outputs.add(pos);
    }

    public void removeOutput(BlockPos pos)
    {
        outputs.remove(pos);
    }
}
