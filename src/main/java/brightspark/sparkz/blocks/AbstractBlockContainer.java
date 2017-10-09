package brightspark.sparkz.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class AbstractBlockContainer<T extends TileEntity> extends BlockBase implements ITileEntityProvider
{
    public AbstractBlockContainer(String name, Material material)
    {
        super(name, material);
        hasTileEntity = true;
    }

    public AbstractBlockContainer(String name)
    {
        super(name);
        hasTileEntity = true;
    }

    @SuppressWarnings("unchecked")
    public T getTileEntity(IBlockAccess world, BlockPos pos)
    {
        return (T) world.getTileEntity(pos);
    }
}
