package brightspark.sparkz.init;

import brightspark.sparkz.blocks.TileCable;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class RegHandler
{
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                SItems.debug,
                new ItemBlock(SBlocks.cable).setRegistryName(SBlocks.cable.getRegistryName())
        );
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(SBlocks.cable);
        GameRegistry.registerTileEntity(TileCable.class, SBlocks.cable.getRegistryName().getResourcePath());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void regModels(ModelRegistryEvent event)
    {
        regModel(SItems.debug);
        regModel(SBlocks.cable);
    }

    @SideOnly(Side.CLIENT)
    private static void regModel(Block block)
    {
        regModel(Item.getItemFromBlock(block));
    }

    @SideOnly(Side.CLIENT)
    private static void regModel(Item item)
    {
        regModel(item, 0);
    }

    @SideOnly(Side.CLIENT)
    private static void regModel(Item item, int meta)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
