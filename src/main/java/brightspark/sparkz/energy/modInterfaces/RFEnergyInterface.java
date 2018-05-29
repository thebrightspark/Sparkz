package brightspark.sparkz.energy.modInterfaces;

import brightspark.sparkz.energy.EnergyType;
import brightspark.sparkz.energy.IEnergy;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class RFEnergyInterface implements IEnergy
{
    private IEnergyHandler energyHandler;

    public RFEnergyInterface(TileEntity te)
    {
        energyHandler = te instanceof IEnergyHandler ? (IEnergyHandler) te : null;
    }

    @Override
    public boolean isValid()
    {
        return energyHandler != null;
    }

    @Override
    public boolean canInput(EnumFacing side)
    {
        return energyHandler instanceof IEnergyReceiver && ((IEnergyReceiver) energyHandler).receiveEnergy(side, 1, true) == 1;
    }

    @Override
    public boolean canOutput(EnumFacing side)
    {
        return energyHandler instanceof IEnergyProvider && ((IEnergyProvider) energyHandler).extractEnergy(side, 1, true) == 1;
    }

    @Override
    public long getMaxInput(EnumFacing side)
    {
        return canInput(side) ? ((IEnergyReceiver) energyHandler).receiveEnergy(side, Integer.MAX_VALUE, true) : 0;
    }

    @Override
    public long getMaxOutput(EnumFacing side)
    {
        return canOutput(side) ? ((IEnergyProvider) energyHandler).extractEnergy(side, Integer.MAX_VALUE, true) : 0;
    }

    @Override
    public long inputEnergy(EnumFacing side, long amount)
    {
        return canInput(side) ? ((IEnergyReceiver) energyHandler).receiveEnergy(side, (int) amount, false) : 0;
    }

    @Override
    public long outputEnergy(EnumFacing side, long maxAmount)
    {
        return canOutput(side) ? ((IEnergyProvider) energyHandler).extractEnergy(side, (int) maxAmount, false) : 0;
    }

    @Override
    public long getEnergyStored()
    {
        return energyHandler.getEnergyStored(null);
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

    @Override
    public EnergyType getEnergyType()
    {
        return EnergyType.REDSTONE_FLUX;
    }
}
