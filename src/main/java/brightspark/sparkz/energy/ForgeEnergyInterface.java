package brightspark.sparkz.energy;

import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyInterface implements IEnergy
{
    private IEnergyStorage energy;
    
    public ForgeEnergyInterface(IEnergyStorage energy)
    {
        this.energy = energy;
    }

    @Override
    public boolean canInput()
    {
        return energy.canReceive();
    }

    @Override
    public boolean canOutput()
    {
        return energy.canExtract();
    }

    @Override
    public int getMaxInput()
    {
        return energy.receiveEnergy(Integer.MAX_VALUE, true);
    }

    @Override
    public int getMaxOutput()
    {
        return energy.extractEnergy(Integer.MAX_VALUE, true);
    }

    @Override
    public int inputEnergy(int amount)
    {
        return energy.receiveEnergy(amount, false);
    }

    @Override
    public int outputEnergy(int maxAmount)
    {
        return energy.extractEnergy(maxAmount, false);
    }
}
