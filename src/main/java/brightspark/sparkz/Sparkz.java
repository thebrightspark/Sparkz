package brightspark.sparkz;

import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.items.ItemDebug;
import brightspark.sparkz.items.ItemWrench;
import brightspark.sparkz.messages.MessageGetComponents;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(modid = Sparkz.MOD_ID, name = Sparkz.MOD_NAME, version = Sparkz.VERSION)
@Mod.EventBusSubscriber
public class Sparkz
{
    public static final String MOD_ID = "sparkz";
    public static final String MOD_NAME = "Sparkz";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(MOD_ID)
    public static Sparkz instance;

    public static Logger logger;
    public static SimpleNetworkWrapper network;

    public static final CreativeTabs TAB = new CreativeTabs(MOD_ID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Blocks.REDSTONE_LAMP);
        }
    };

    public static final ItemDebug debug = new ItemDebug();
    public static final ItemWrench wrench = new ItemWrench();
    public static final BlockCable cable = new BlockCable();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        network.registerMessage(MessageGetComponents.Handler.class, MessageGetComponents.class, 0, Side.CLIENT);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
            debug,
            wrench,
            new ItemBlock(cable).setRegistryName(cable.getRegistryName())
        );
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(cable);
        GameRegistry.registerTileEntity(TileCable.class, cable.getRegistryName().getResourcePath());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void regModels(ModelRegistryEvent event)
    {
        regModel(debug);
        regModel(wrench);
        regModel(cable);
    }

    @SideOnly(Side.CLIENT)
    private static void regModel(Block block)
    {
        regModel(Item.getItemFromBlock(block));
    }

    @SideOnly(Side.CLIENT)
    private static void regModel(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

}
