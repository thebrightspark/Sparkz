package brightspark.sparkz.blocks;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.energy.EnergyNetwork;
import brightspark.sparkz.energy.IEnergy;
import brightspark.sparkz.energy.NetworkData;
import brightspark.sparkz.util.CommonUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
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
        markDirty();
        //Sparkz.logger.info("Set IO for cable {} on side {} to {}", pos, side, io);
    }

    public ECableIO getSideIO(EnumFacing side)
    {
        return sideIO.computeIfAbsent(side, s -> ECableIO.NONE);
    }

    public void setNetwork(EnergyNetwork network)
    {
        this.network = network;
        markDirty();
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
        if(world.getBlockState(neighbourPos).getBlock() == Sparkz.cable)
        {
            setSideIO(side, ECableIO.NEUTRAL);
            return;
        }
        EnumFacing sideOpposite = side.getOpposite();
        IEnergy neighbourEnergy = IEnergy.create(world, neighbourPos, sideOpposite);
        ECableIO io = ECableIO.NONE;
        if(neighbourEnergy != null)
        {
            boolean canInput = neighbourEnergy.canInput(sideOpposite);
            if(neighbourEnergy.canOutput(sideOpposite))
            {
                if(canInput)
                    io = ECableIO.NEUTRAL;
                else
                    io = ECableIO.INPUT;
            }
            else if(canInput)
                io = ECableIO.OUTPUT;
        }
        setSideIO(side, io);
    }

    @Override
    public void onLoad()
    {
        for(EnumFacing side : EnumFacing.VALUES)
            determineSideIO(world, pos.offset(side));
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
        NBTTagList list = new NBTTagList();
        sideIO.forEach((side, io) -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("side", (byte) side.getIndex());
            tag.setByte("io", (byte) io.ordinal());
            list.appendTag(tag);
        });
        nbt.setTag("io_list", list);
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

        sideIO.clear();
        NBTTagList list = nbt.getTagList("io_list", Constants.NBT.TAG_COMPOUND);
        list.forEach(tag -> {
            NBTTagCompound tagC = (NBTTagCompound) tag;
            EnumFacing side = EnumFacing.VALUES[tagC.getByte("side")];
            ECableIO io = ECableIO.getFromIndex(tagC.getByte("io"));
            if(side == null || io == null)
                Sparkz.logger.info("Invalid side or io from NBT for cable at %s -> side: %s, io: %s", pos, side, io);
            else
                sideIO.put(side, io);
        });
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        Sparkz.logger.info("CABLE TE AT {} -> getUpdatePacket", pos);
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        Sparkz.logger.info("CABLE TE AT {} -> getUpdateTag", pos);
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        Sparkz.logger.info("CABLE TE AT {} -> onDataPacket", pos);
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        Sparkz.logger.info("CABLE TE AT {} -> handleUpdateTag", pos);
        readFromNBT(tag);
    }
}
