package brightspark.sparkz.util;

import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.energy.EnergyNetwork;
import com.sun.istack.internal.NotNull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

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
        return xDif == 1 || yDif == 1 || zDif == 1;
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
     * Finds an adjacent cable block
     */
    public static EnumFacing findAdjacentCable(IBlockAccess world, BlockPos pos)
    {
        for(EnumFacing side : EnumFacing.values())
        {
            IBlockState state = world.getBlockState(pos.offset(side));
            if(state.getBlock() instanceof BlockCable)
                return side;
        }
        return null;
    }

    /**
     * Finds an adjacent network that's not one of the given energy networks
     */
    public static EnergyNetwork findAdjacentNetwork(IBlockAccess world, BlockPos pos, @NotNull EnergyNetwork... networks)
    {
        for(EnumFacing side : EnumFacing.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(side));
            if(te != null && te instanceof TileCable)
                for(EnergyNetwork network : networks)
                {
                    EnergyNetwork otherNetwork = ((TileCable) te).getNetwork();
                    if(!otherNetwork.equals(network))
                        return otherNetwork;
                }
        }
        return null;
    }
}
