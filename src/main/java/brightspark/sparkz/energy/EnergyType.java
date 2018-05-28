package brightspark.sparkz.energy;

import static brightspark.sparkz.SparkzConfig.ENERGY_CONVERSION;

public enum EnergyType
{
    FORGE_ENERGY(ENERGY_CONVERSION.forge_energy),
    REDSTONE_FLUX(ENERGY_CONVERSION.redstone_flux),
    TESLA(ENERGY_CONVERSION.tesla),
    INDUSTRIALCRAFT(ENERGY_CONVERSION.industrialcraft),
    BUILDCRAFT(ENERGY_CONVERSION.buildcraft),
    GALACTICRAFT(ENERGY_CONVERSION.galacticraft),
    MEKANISM(ENERGY_CONVERSION.mekanism);

    private final double ratio;

    EnergyType(double ratio)
    {
        this.ratio = ratio;
    }

    public long convertTo(long amount, EnergyType otherType)
    {
        return ratio == otherType.ratio ? amount : (long) (((double) amount / ratio) * otherType.ratio);
    }
}
