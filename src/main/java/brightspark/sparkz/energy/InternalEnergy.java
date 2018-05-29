package brightspark.sparkz.energy;

import net.minecraft.util.EnumFacing;

public class InternalEnergy
{
    public double amount;

    public InternalEnergy() {}

    public void addMaxOutput(IEnergy energy, EnumFacing side)
    {
        amount += energy.getMaxOutputType(side, EnergyType.INTERNAL_ENERGY);
    }

    public long convertTo(EnergyType type)
    {
        return (long) EnergyType.INTERNAL_ENERGY.convertTo(amount, type);
    }
}
