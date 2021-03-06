package brightspark.sparkz.energy.modInterfaces;

import brightspark.sparkz.energy.EnergyType;
import brightspark.sparkz.energy.IEnergy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyInterface implements IEnergy
{
    private IEnergyStorage energy;
    
    public ForgeEnergyInterface(TileEntity te, EnumFacing side)
    {
        energy = te.getCapability(CapabilityEnergy.ENERGY, side);
    }

    @Override
    public boolean isValid()
    {
        return energy != null;
    }

    @Override
    public boolean canInput(EnumFacing side)
    {
        return energy.canReceive();
    }

    @Override
    public boolean canOutput(EnumFacing side)
    {
        return energy.canExtract();
    }

    @Override
    public long getMaxInput(EnumFacing side)
    {
        return energy.receiveEnergy(Integer.MAX_VALUE, true);
    }

    @Override
    public long getMaxOutput(EnumFacing side)
    {
        return energy.extractEnergy(Integer.MAX_VALUE, true);
    }

    @Override
    public long inputEnergy(EnumFacing side, long amount)
    {
        return energy.receiveEnergy((int) amount, false);
    }

    @Override
    public long outputEnergy(EnumFacing side, long maxAmount)
    {
        return energy.extractEnergy((int) maxAmount, false);
    }

    @Override
    public long getEnergyStored()
    {
        return energy.getEnergyStored();
    }

    @Override
    public long getMaxEnergyStored()
    {
        return energy.getMaxEnergyStored();
    }

    @Override
    public EnergyType getEnergyType()
    {
        return EnergyType.FORGE_ENERGY;
    }
}
