package brightspark.sparkz.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public interface IEnergy
{
    static IEnergy create(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        TileEntity te = world.getTileEntity(pos);
        return te == null ? null : create(te, side);
    }

    static IEnergy create(TileEntity te, EnumFacing side)
    {
        //Try get Forge Energy
        IEnergyStorage forgeEnergy = te.getCapability(CapabilityEnergy.ENERGY, side);
        if(forgeEnergy != null) return new ForgeEnergyInterface(forgeEnergy);

        //TODO: RF

        //TODO: Tesla

        //TODO: EU

        //TODO: BuildCraft MJ

        //TODO: Galacticraft Energy

        return null;
    }

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
    int getMaxInput();

    /**
     * Returns the max amount this can provide
     */
    int getMaxOutput();

    /**
     * Gives energy to this
     * @return The amount of energy this actually accepted
     */
    int inputEnergy(int amount);

    /**
     * Takes energy from this
     * @return The amount of energy this actually provided
     */
    int outputEnergy(int maxAmount);
}
