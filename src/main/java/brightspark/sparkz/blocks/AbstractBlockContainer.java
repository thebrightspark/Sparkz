package brightspark.sparkz.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public abstract class AbstractBlockContainer extends BlockBase implements ITileEntityProvider
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

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
}
