package brightspark.sparkz.init;

import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.blocks.TileCable;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public class SBlocks
{
    public static List<Block> BLOCKS = new ArrayList<>();
    public static List<ItemBlock> ITEM_BLOCKS = new ArrayList<>();

    public static BlockCable cable;

    static
    {
        addBlock(cable = new BlockCable());
    }

    private static void addBlock(Block block)
    {
        addBlock(block, (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    private static void addBlock(Block block, ItemBlock itemBlock)
    {
        BLOCKS.add(block);
        ITEM_BLOCKS.add(itemBlock);
    }

    public static ItemBlock[] getItemBlocks()
    {
        return ITEM_BLOCKS.toArray(new ItemBlock[ITEM_BLOCKS.size()]);
    }

    public static Block[] getBlocks()
    {
        return BLOCKS.toArray(new Block[BLOCKS.size()]);
    }

    public static void uninitLists()
    {
        BLOCKS = null;
        ITEM_BLOCKS = null;
    }

    public static void regTileEntities()
    {
        GameRegistry.registerTileEntity(TileCable.class, cable.getRegistryName().getResourcePath());
    }
}
