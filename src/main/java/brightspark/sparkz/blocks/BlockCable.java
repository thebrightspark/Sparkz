package brightspark.sparkz.blocks;

import brightspark.sparkz.energy.EnergyHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockCable extends BlockBase
{
    //TODO: Use these cables as sided input/output blocks
    //  When block is placed adjacent or placed adjacent to an energy block, try to determine whether input or output is appropriate
    //  Default to output if can't decide

    public BlockCable()
    {
        super("cable");
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosionIn)
    {
        super.onBlockDestroyedByExplosion(world, pos, explosionIn);
        EnergyHandler.onCableDestroyed(world, pos);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        super.onBlockHarvested(world, pos, state, player);
        EnergyHandler.onCableDestroyed(world, pos);
    }
}
