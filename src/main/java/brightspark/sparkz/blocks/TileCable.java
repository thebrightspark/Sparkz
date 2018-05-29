package brightspark.sparkz.blocks;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.energy.EnergyNetwork;
import brightspark.sparkz.energy.IEnergy;
import brightspark.sparkz.energy.NetworkData;
import brightspark.sparkz.util.CommonUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class TileCable extends TileEntity
{
    private Map<EnumFacing, ECableIO> sideIO = new HashMap<>(6);
    private EnergyNetwork network;

    public TileCable()
    {
        //Init side IO map
        for(EnumFacing side : EnumFacing.values())
            sideIO.put(side, ECableIO.NONE);
    }

    public void setSideIO(EnumFacing side, ECableIO io)
    {
        sideIO.put(side, io);
    }

    public ECableIO getSideIO(EnumFacing side)
    {
        return sideIO.get(side);
    }

    public void setNetwork(EnergyNetwork network)
    {
        this.network = network;
    }

    public EnergyNetwork getNetwork()
    {
        return network;
    }

    /**
     * Sets the side that the neighbour block is on to the most appropriate IO setting
     * This is called in BlockCable#onNeighborChanged
     */
    public void determineSideIO(IBlockAccess world, BlockPos neighbourPos)
    {
        EnumFacing side = CommonUtils.getSide(pos, neighbourPos);

        if(world.isAirBlock(neighbourPos))
        {
            setSideIO(side, ECableIO.NONE);
            return;
        }
        EnumFacing sideOpposite = side.getOpposite();
        IEnergy neighbourEnergy = IEnergy.create(world, neighbourPos, sideOpposite);
        ECableIO io = ECableIO.NONE;
        if(neighbourEnergy != null)
        {
            if(neighbourEnergy.canOutput(sideOpposite))
                io = ECableIO.INPUT;
            else if(neighbourEnergy.canInput(sideOpposite))
                io = ECableIO.OUTPUT;
        }
        setSideIO(side, io);
    }

    @Override
    protected void setWorldCreate(World worldIn)
    {
        setWorld(worldIn);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if(network != null)
            nbt.setUniqueId("network", network.getUuid());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if(nbt.hasKey("network"))
            network = NetworkData.getNetwork(world, nbt.getUniqueId("network"));
        else
            //Try see if this cable is in an existing network instead
             network = NetworkData.getNetworkWithCable(world, pos);
        Sparkz.logger.info("---------- Loading network for cable at {} -> {}", pos, network);
    }
}
