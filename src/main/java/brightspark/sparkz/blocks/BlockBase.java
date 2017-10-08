package brightspark.sparkz.blocks;

import brightspark.sparkz.Sparkz;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBase extends Block
{
    public BlockBase(String name, Material material)
    {
        super(material);
        setRegistryName(name);
        setUnlocalizedName(name);
        setCreativeTab(Sparkz.TAB);
        setHardness(1f);
        setResistance(10f);
    }

    public BlockBase(String name)
    {
        this(name, Material.GROUND);
    }
}
