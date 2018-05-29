package brightspark.sparkz.energy.modInterfaces;

import brightspark.sparkz.energy.EnergyType;
import brightspark.sparkz.energy.IEnergy;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TeslaEnergyInterface implements IEnergy
{
    private ITeslaHolder energyHolder;
    private ITeslaConsumer energyInput;
    private ITeslaProducer energyOutput;

    public TeslaEnergyInterface(TileEntity te, EnumFacing side)
    {
        energyHolder = te.getCapability(TeslaCapabilities.CAPABILITY_HOLDER, side);
        energyInput = te.getCapability(TeslaCapabilities.CAPABILITY_CONSUMER, side);
        energyOutput = te.getCapability(TeslaCapabilities.CAPABILITY_PRODUCER, side);
    }

    private boolean hasStorage()
    {
        return energyHolder != null;
    }

    @Override
    public boolean isValid()
    {
        return energyInput != null || energyOutput != null;
    }

    @Override
    public boolean canInput(EnumFacing side)
    {
        return energyInput != null;
    }

    @Override
    public boolean canOutput(EnumFacing side)
    {
        return energyOutput != null;
    }

    @Override
    public long getMaxInput(EnumFacing side)
    {
        return canInput(side) ? energyInput.givePower(Long.MAX_VALUE, true) : 0L;
    }

    @Override
    public long getMaxOutput(EnumFacing side)
    {
        return canOutput(side) ? energyOutput.takePower(Long.MAX_VALUE, true) : 0L;
    }

    @Override
    public long inputEnergy(EnumFacing side, long amount)
    {
        return canInput(side) ? energyInput.givePower(amount, false) : 0L;
    }

    @Override
    public long outputEnergy(EnumFacing side, long maxAmount)
    {
        return canOutput(side) ? energyOutput.takePower(maxAmount, false) : 0L;
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

    @Override
    public EnergyType getEnergyType()
    {
        return EnergyType.TESLA;
    }
}
