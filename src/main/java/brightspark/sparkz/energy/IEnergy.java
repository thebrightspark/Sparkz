package brightspark.sparkz.energy;

import brightspark.sparkz.energy.modInterfaces.ForgeEnergyInterface;
import brightspark.sparkz.energy.modInterfaces.RFEnergyInterface;
import brightspark.sparkz.energy.modInterfaces.TeslaEnergyInterface;
import brightspark.sparkz.util.OtherMods;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IEnergy
{
    static IEnergy create(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        TileEntity te = world.getTileEntity(pos);
        return te == null ? null : create(te, side);
    }

    static IEnergy create(TileEntity te, EnumFacing side)
    {
        IEnergy energy;

        //RF
        if(OtherMods.isLoaded(OtherMods.RF))
        {
            energy = new RFEnergyInterface(te);
            if(energy.isValid()) return energy;
        }

        //Tesla
        if(OtherMods.isLoaded(OtherMods.TESLA))
        {
            energy = new TeslaEnergyInterface(te, side);
            if(energy.isValid()) return energy;
        }

        //TODO: EU

        //TODO: BuildCraft MJ

        //TODO: Galacticraft Energy

        //Forge Energy
        energy = new ForgeEnergyInterface(te, side);
        if(energy.isValid()) return energy;

        return null;
    }

    /**
     * Returns if the interface was created successfully
     */
    boolean isValid();

    /**
     * Returns if this can accept energy
     */
    boolean canInput(EnumFacing side);

    /**
     * Returns if this can provide energy
     */
    boolean canOutput(EnumFacing side);

    /**
     * Returns the max amount this can accept
     */
    long getMaxInput(EnumFacing side);

    /**
     * Returns the max amount this can provide
     */
    long getMaxOutput(EnumFacing side);

    /**
     * Returns the max output this can provide in the given energy type
     */
    default double getMaxOutputType(EnumFacing side, EnergyType type)
    {
        return getEnergyType().convertTo(getMaxOutput(side), type);
    }

    /**
     * Gives energy to this
     * @return The amount of energy this actually accepted
     */
    long inputEnergy(EnumFacing side, long amount);

    /**
     * Takes energy from this
     * @return The amount of energy this actually provided
     */
    long outputEnergy(EnumFacing side, long maxAmount);

    /**
     * Returns the amount of energy stored
     */
    long getEnergyStored();

    /**
     * Returns the amount of energy stored in the given energy type
     */
    default double getEnergyStored(EnergyType type)
    {
        return getEnergyType().convertTo(getEnergyStored(), type);
    }

    /**
     * Returns the max amount of energy this can store
     */
    long getMaxEnergyStored();

    /**
     * Returns the energy type
     */
    EnergyType getEnergyType();
}
