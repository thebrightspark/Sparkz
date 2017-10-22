package brightspark.sparkz.energy.modInterfaces;

import brightspark.sparkz.energy.IEnergy;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;

public class TeslaEnergyInterface implements IEnergy
{
    private ITeslaHolder energyHolder;
    private ITeslaConsumer energyInput;
    private ITeslaProducer energyOutput;

    public TeslaEnergyInterface() {}

    public TeslaEnergyInterface setHolder(ITeslaHolder energyHolder)
    {
        this.energyHolder = energyHolder;
        return this;
    }

    public TeslaEnergyInterface setConsumer(ITeslaConsumer energyConsumer)
    {
        energyInput = energyConsumer;
        return this;
    }

    public TeslaEnergyInterface setProducer(ITeslaProducer energyProducer)
    {
        energyOutput = energyProducer;
        return this;
    }

    public boolean hasIO()
    {
        return energyInput != null || energyOutput != null;
    }

    private boolean hasStorage()
    {
        return energyHolder != null;
    }

    @Override
    public boolean canInput()
    {
        return energyInput != null;
    }

    @Override
    public boolean canOutput()
    {
        return energyOutput != null;
    }

    @Override
    public long getMaxInput()
    {
        return canInput() ? energyInput.givePower(Long.MAX_VALUE, true) : 0L;
    }

    @Override
    public long getMaxOutput()
    {
        return canOutput() ? energyOutput.takePower(Long.MAX_VALUE, true) : 0L;
    }

    @Override
    public long inputEnergy(long amount)
    {
        return canInput() ? energyInput.givePower(amount, false) : 0L;
    }

    @Override
    public long outputEnergy(long maxAmount)
    {
        return canOutput() ? energyOutput.takePower(maxAmount, false) : 0L;
    }

    @Override
    public long getEnergyStored()
    {
        return hasStorage() ? energyHolder.getStoredPower() : 0L;
    }

    @Override
    public long getMaxEnergyStored()
    {
        return hasStorage() ? energyHolder.getCapacity() : 0L;
    }
}
