package brightspark.sparkz.util;

import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.energy.EnergyNetwork;
import brightspark.sparkz.energy.IEnergy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.HashSet;
import java.util.Set;

public class CommonUtils
{
    /**
     * Returns whether the two block positions are adjacent to each other
     */
    public static boolean areBlocksAdjacent(BlockPos pos1, BlockPos pos2)
    {
        int xDif = Math.abs(pos1.getX() - pos2.getX());
        int yDif = Math.abs(pos1.getY() - pos2.getY());
        int zDif = Math.abs(pos1.getZ() - pos2.getZ());
        boolean result = xDif + yDif + zDif == 1;
        //Sparkz.logger.info("Are adjacent? {} -> Pos1: {}, Pos2: {}", result, pos1, pos2);
        return result;
    }

    /**
     * Returns whether cable can be connected to the block at the given position
     */
    public static boolean canCableConnect(IBlockAccess world, BlockPos pos)
    {
        return isCable(world, pos) || isIO(world, pos);
    }

    /**
     * Returns the amount of adjacent cables to this position
     */
    public static int countAdjacentCables(IBlockAccess world, BlockPos pos)
    {
        int num = 0;
        for(EnumFacing side : EnumFacing.values())
        {
            IBlockState state = world.getBlockState(pos.offset(side));
            if(state.getBlock() instanceof BlockCable)
                num++;
        }
        return num;
    }

    public static boolean isCable(IBlockAccess world, BlockPos pos)
    {
        return world.getBlockState(pos).getBlock() instanceof BlockCable;
    }

    public static boolean isIO(IBlockAccess world, BlockPos pos)
    {
        IEnergy energy = IEnergy.create(world, pos, null);
        return energy != null && (energy.canInput() || energy.canOutput());
    }

    /**
     * Finds an adjacent network
     */
    public static Set<EnergyNetwork> findAdjacentNetworks(IBlockAccess world, BlockPos pos)
    {
        Set<EnergyNetwork> adjacentNetworks = new HashSet<>();
        for(EnumFacing side : EnumFacing.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(side));
            if(te instanceof TileCable)
            {
                EnergyNetwork otherNetwork = ((TileCable) te).getNetwork();
                adjacentNetworks.add(otherNetwork);
            }
        }
        return adjacentNetworks;
    }

    public static Set<Set<BlockPos>> getAllAdjacentConnectedCables(IBlockAccess world, BlockPos pos)
    {
        Set<Set<BlockPos>> networks = new HashSet<>();
        for(EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos nextPos = pos.offset(facing);
            if(!CommonUtils.isCable(world, nextPos))
                continue;
            boolean alreadyInNetwork = false;
            for(Set<BlockPos> network : networks)
            {
                if(network.contains(nextPos))
                {
                    alreadyInNetwork = true;
                    break;
                }
            }
            if(alreadyInNetwork)
                continue;
            Set<BlockPos> connectedCables = getAllAdjacentConnectedCables(world, new HashSet<>(), nextPos);
            connectedCables.add(nextPos);
            if(!connectedCables.isEmpty())
                networks.add(connectedCables);
        }
        return networks;
    }

    public static Set<BlockPos> getAllConnectedCables(IBlockAccess world, BlockPos pos)
    {
        Set<BlockPos> cables = new HashSet<>();
        if(!isCable(world, pos)) return cables;
        cables.add(pos);
        return getAllAdjacentConnectedCables(world, cables, pos);
    }

    private static Set<BlockPos> getAllAdjacentConnectedCables(IBlockAccess world, Set<BlockPos> cables, BlockPos pos)
    {
        for(EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos nextPos = pos.offset(facing);
            if(isCable(world, nextPos) && !cables.contains(nextPos))
            {
                cables.add(nextPos);
                getAllAdjacentConnectedCables(world, cables, nextPos);
            }
        }
        return cables;
    }
}
