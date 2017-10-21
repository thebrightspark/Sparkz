package brightspark.sparkz.util;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.energy.EnergyNetwork;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.List;

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
        return canCableConnect(world.getBlockState(pos));
    }

    /**
     * Returns whether cable can be connected to the given block state
     */
    public static boolean canCableConnect(IBlockState state)
    {
        return state.getBlock() instanceof BlockCable;
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

    /**
     * Finds an adjacent network that's not one of the given energy networks
     */
    public static List<EnergyNetwork> findAdjacentNetworks(IBlockAccess world, BlockPos pos, EnergyNetwork... networks)
    {
        List<EnergyNetwork> adjacentNetworks = new ArrayList<>();
        for(EnumFacing side : EnumFacing.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(side));
            if(te != null && te instanceof TileCable)
            {
                EnergyNetwork otherNetwork = ((TileCable) te).getNetwork();
                boolean isBlacklistedNetwork = false;
                for(EnergyNetwork network : networks)
                    if(otherNetwork.equals(network))
                    {
                        isBlacklistedNetwork = true;
                        break;
                    }
                if(!isBlacklistedNetwork && !adjacentNetworks.contains(otherNetwork))
                    adjacentNetworks.add(otherNetwork);
            }
        }
        return adjacentNetworks;
    }

    public static List<List<BlockPos>> getAllAdjacentConnectedCables(IBlockAccess world, BlockPos pos)
    {
        List<List<BlockPos>> networks = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos nextPos = pos.offset(facing);
            if(!CommonUtils.isCable(world, nextPos))
                continue;
            boolean alreadyInNetwork = false;
            for(List<BlockPos> network : networks)
            {
                if(network.contains(nextPos))
                {
                    alreadyInNetwork = true;
                    break;
                }
            }
            if(alreadyInNetwork)
                continue;
            List<BlockPos> connectedCables = getAllAdjacentConnectedCables(world, new ArrayList<>(), nextPos);
            connectedCables.add(nextPos);
            if(!connectedCables.isEmpty())
                networks.add(connectedCables);
        }
        return networks;
    }

    public static List<BlockPos> getAllConnectedCables(IBlockAccess world, BlockPos pos)
    {
        List<BlockPos> cables = new ArrayList<>();
        if(!isCable(world, pos)) return cables;
        cables.add(pos);
        return getAllAdjacentConnectedCables(world, cables, pos);
    }

    private static List<BlockPos> getAllAdjacentConnectedCables(IBlockAccess world, List<BlockPos> cables, BlockPos pos)
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
