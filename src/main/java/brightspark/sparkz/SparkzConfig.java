package brightspark.sparkz;

import net.minecraftforge.common.config.Config;

@Config(modid = Sparkz.MOD_ID)
public class SparkzConfig
{
    @Config.Name("Energy Conversion")
    @Config.Comment("Energy conversion configs")
    @Config.RequiresMcRestart
    public static final EnergyConversion ENERGY_CONVERSION = new EnergyConversion();

    public static class EnergyConversion
    {
        @Config.Name("Forge Energy (FE)")
        @Config.RangeDouble(min = 0D)
        public double forge_energy = 1D;

        @Config.Name("Redstone Flux (RF)")
        @Config.RangeDouble(min = 0D)
        public double redstone_flux = 1D;

        @Config.Name("Tesla (T)")
        @Config.RangeDouble(min = 0D)
        public double tesla = 1D;

        @Config.Name("IndustrialCraft (EU)")
        @Config.RangeDouble(min = 0D)
        public double industrialcraft = 0.25D;

        @Config.Name("BuildCraft (MJ)")
        @Config.RangeDouble(min = 0D)
        public double buildcraft = 0.1D;

        @Config.Name("Galacticraft (gJ)")
        @Config.RangeDouble(min = 0D)
        public double galacticraft = 1.6D;

        @Config.Name("Mekanism (J)")
        @Config.RangeDouble(min = 0D)
        public double mekanism = 2.5D;
    }
}
