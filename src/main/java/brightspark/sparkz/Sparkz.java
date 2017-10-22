package brightspark.sparkz;

import brightspark.sparkz.init.SBlocks;
import brightspark.sparkz.init.SItems;
import brightspark.sparkz.messages.MessageGetComponents;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = Sparkz.MOD_ID, name = Sparkz.MOD_NAME, version = Sparkz.VERSION)
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

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        network.registerMessage(MessageGetComponents.Handler.class, MessageGetComponents.class, 0, Side.CLIENT);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        SItems.uninitLists();
        SBlocks.uninitLists();
    }
}
