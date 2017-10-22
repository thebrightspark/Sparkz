package brightspark.sparkz.energy.modInterfaces;

import brightspark.sparkz.energy.IEnergy;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import net.minecraft.util.EnumFacing;

public class RFEnergyInterface implements IEnergy
{
    private IEnergyHandler energyHandler;

    public RFEnergyInterface(IEnergyHandler energyHandler)
    {
        this.energyHandler = energyHandler;
    }

    @Override
    public boolean canInput()
    {
        return energyHandler instanceof IEnergyReceiver;
    }

    @Override
    public boolean canOutput()
    {
        return energyHandler instanceof IEnergyProvider;
    }

    @Override
    public long getMaxInput()
    {
        return getMaxInput(null);
    }

    public long getMaxInput(EnumFacing side)
    {
        return canInput() ? ((IEnergyReceiver) energyHandler).receiveEnergy(side, Integer.MAX_VALUE, true) : 0;
    }

    @Override
    public long getMaxOutput()
    {
        return getMaxOutput(null);
    }

    public long getMaxOutput(EnumFacing side)
    {
        return canOutput() ? ((IEnergyProvider) energyHandler).extractEnergy(side, Integer.MAX_VALUE, true) : 0;
    }

    @Override
    public long inputEnergy(long amount)
    {
        return inputEnergy((int) amount, null);
    }

    public int inputEnergy(int amount, EnumFacing side)
    {
        return canInput() ? ((IEnergyReceiver) energyHandler).receiveEnergy(side, amount, false) : 0;
    }

    @Override
    public long outputEnergy(long maxAmount)
    {
        return outputEnergy((int) maxAmount, null);
    }

    public int outputEnergy(int maxAmount, EnumFacing side)
    {
        return canOutput() ? ((IEnergyProvider) energyHandler).extractEnergy(side, maxAmount, false) : 0;
    }

    @Override
    public long getEnergyStored()
    {
        return getEnergyStored(null);
    }

    public long getEnergyStored(EnumFacing side)
    {
        return energyHandler.getEnergyStored(side);
    }

    @Override
    public long getMaxEnergyStored()
    {
        return getMaxEnergyStored(null);
    }

    public long getMaxEnergyStored(EnumFacing side)
    {
        return energyHandler.getMaxEnergyStored(side);
    }
}
