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
        Sparkz.logger.info("Are adjacent? {} -> Pos1: {}, Pos2: {}", result, pos1, pos2);
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
}
