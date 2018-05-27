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
        //Forge Energy
        IEnergy energy = new ForgeEnergyInterface(te, side);
        if(energy.isValid()) return energy;

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

        return null;
    }

    /**
     * Returns if the interface was created successfully
     */
    boolean isValid();

    /**
     * Returns if this can accept energy
     */
    boolean canInput();

    /**
     * Returns if this can provide energy
     */
    boolean canOutput();

    /**
     * Returns the max amount this can accept
     */
    long getMaxInput();

    /**
     * Returns the max amount this can provide
     */
    long getMaxOutput();

    /**
     * Gives energy to this
     * @return The amount of energy this actually accepted
     */
    long inputEnergy(long amount);

    /**
     * Takes energy from this
     * @return The amount of energy this actually provided
     */
    long outputEnergy(long maxAmount);

    /**
     * Returns the amount of energy stored
     */
    long getEnergyStored();

    /**
     * Returns the max amount of energy this can store
     */
    long getMaxEnergyStored();
}
