package brightspark.sparkz;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {

    }
}
