package brightspark.sparkz.energy;

import brightspark.sparkz.energy.modInterfaces.ForgeEnergyInterface;
import brightspark.sparkz.energy.modInterfaces.RFEnergyInterface;
import brightspark.sparkz.energy.modInterfaces.TeslaEnergyInterface;
import brightspark.sparkz.util.ModIds;
import cofh.redstoneflux.api.IEnergyHandler;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

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
        IEnergyStorage forgeEnergy = te.getCapability(CapabilityEnergy.ENERGY, side);
        if(forgeEnergy != null) return new ForgeEnergyInterface(forgeEnergy);

        //RF
        if(Loader.isModLoaded(ModIds.RF))
        {
            RFEnergyInterface rf = getRF(te);
            if(rf != null) return rf;
        }

        //Tesla
        if(Loader.isModLoaded(ModIds.TESLA))
        {
            TeslaEnergyInterface tesla = getTesla(te, side);
            if(tesla != null) return tesla;
        }

        //TODO: EU

        //TODO: BuildCraft MJ

        //TODO: Galacticraft Energy

        return null;
    }

    @Optional.Method(modid = ModIds.RF)
    static RFEnergyInterface getRF(TileEntity te)
    {
        return te instanceof IEnergyHandler ? new RFEnergyInterface((IEnergyHandler) te) : null;
    }

    @Optional.Method(modid = ModIds.TESLA)
    static TeslaEnergyInterface getTesla(TileEntity te, EnumFacing side)
    {
        TeslaEnergyInterface tesla = new TeslaEnergyInterface();

        ITeslaHolder holder = te.getCapability(TeslaCapabilities.CAPABILITY_HOLDER, side);
        if(holder != null) tesla.setHolder(holder);
        ITeslaConsumer consumer = te.getCapability(TeslaCapabilities.CAPABILITY_CONSUMER, side);
        if(consumer != null) tesla.setConsumer(consumer);
        ITeslaProducer producer = te.getCapability(TeslaCapabilities.CAPABILITY_PRODUCER, side);
        if(producer != null) tesla.setProducer(producer);

        return tesla.hasIO() ? tesla : null;
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
