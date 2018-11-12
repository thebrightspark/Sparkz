package brightspark.sparkz.blocks;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.energy.EnergyNetwork;
import brightspark.sparkz.energy.IEnergy;
import brightspark.sparkz.energy.NetworkData;
import brightspark.sparkz.util.CommonUtils;
import brightspark.sparkz.util.Pair;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockCable extends AbstractBlockContainer<TileCable>
{
    //https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/2face865be2516c59af77d2b93fb98bc22bbd71e/src/main/java/com/choonster/testmod3/block/pipe/BlockPipeBase.java
    //http://www.minecraftforge.net/forum/topic/34236-18-pipes/
    public static final AxisAlignedBB CENTER_BOX = new AxisAlignedBB(0.3125f, 0.3125f, 0.3125f, 0.6875f, 0.6875f, 0.6875f);
    public static final ImmutableList<AxisAlignedBB> CONNECTED_BOXES = ImmutableList.of(
            new AxisAlignedBB(0.4375d, 0d, 0.4375d, 0.5625d, 0.375d, 0.5625d), //Down
            new AxisAlignedBB(0.4375d, 0.625d, 0.4375d, 0.5625d, 1d, 0.5625d), //Up
            new AxisAlignedBB(0.4375d, 0.4375d, 0d, 0.5625d, 0.5625d, 0.375d), //North
            new AxisAlignedBB(0.4375d, 0.4375d, 0.625d, 0.5625d, 0.5625d, 1d), //South
            new AxisAlignedBB(0d, 0.4375d, 0.4375d, 0.375d, 0.5625d, 0.5625d), //West
            new AxisAlignedBB(0.625d, 0.4375d, 0.4375d, 1d, 0.5625d, 0.5625d)  //East
    );
    public static final ImmutableList<PropertyEnum<ECableIO>> IO_PROPERTIES = ImmutableList.copyOf(
        Stream.of(EnumFacing.VALUES)
            .map(facing -> PropertyEnum.create(facing.getName(), ECableIO.class))
            .collect(Collectors.toList()));

    public BlockCable()
    {
        super("cable");
        IBlockState defaultState = blockState.getBaseState();
        for(PropertyEnum<ECableIO> prop : IO_PROPERTIES)
            defaultState = defaultState.withProperty(prop, ECableIO.NONE);
        setDefaultState(defaultState);
    }

    private boolean isConnected(IBlockState state, EnumFacing facing)
    {
        return state.getValue(IO_PROPERTIES.get(facing.getIndex())) != ECableIO.NONE;
    }

    private AxisAlignedBB getConnectionBox(EnumFacing facing)
    {
        return CONNECTED_BOXES.get(facing.getIndex());
    }

    /**
     * Gets all bounding boxes for current connections
     */
    private Set<AxisAlignedBB> getBoundingBoxesForState(IBlockState state)
    {
        Set<AxisAlignedBB> boxes = Arrays.stream(EnumFacing.VALUES).filter(facing -> isConnected(state, facing)).map(this::getConnectionBox).collect(Collectors.toSet());
        boxes.add(CENTER_BOX);
        return boxes;
    }

    private Vec3d getPlayerEyePos(EntityPlayer player)
    {
        return player.getPositionEyes(player instanceof EntityPlayerSP ? Minecraft.getMinecraft().getRenderPartialTicks() : 1F);
    }

    private Pair<EnumFacing, AxisAlignedBB> rayTraceBoxes(EntityPlayer player, IBlockState actualState, BlockPos pos)
    {
        Vec3d start = getPlayerEyePos(player);
        double dist = start.distanceTo(new Vec3d(pos));
        Vec3d end = start.add(player.getLookVec().scale(dist + 1.5D));
        EnumFacing closestFacing = null;
        AxisAlignedBB closestBox = CENTER_BOX;
        double closestDist = Double.MAX_VALUE;

        RayTraceResult ray = rayTrace(pos, start, end, CENTER_BOX);
        if(ray != null)
            closestDist = ray.hitVec.squareDistanceTo(start);

        for(EnumFacing facing : EnumFacing.VALUES)
        {
            if(isConnected(actualState, facing))
            {
                AxisAlignedBB box = getConnectionBox(facing);
                ray = rayTrace(pos, start, end, box);
                if(ray != null)
                {
                    double rayDist = ray.hitVec.squareDistanceTo(start);
                    if(rayDist < closestDist)
                    {
                        closestFacing = facing;
                        closestBox = box;
                        closestDist = rayDist;
                    }
                }
            }
        }

        return new Pair<>(closestFacing, closestBox);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileCable();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        if(heldItem.getItem() != Sparkz.wrench)
            return false;
        TileCable te = getTileEntity(worldIn, pos);
        if(te == null)
            return false;
        if(!worldIn.isRemote)
            return true;
        EnergyNetwork network = te.getNetwork();

        if(playerIn.isSneaking())
            //Break block
            if(removedByPlayer(state, worldIn, pos, playerIn, false))
                //Remove from network
                network.removeCable(pos);
        else
        {
            IBlockState actualState = getActualState(state, worldIn, pos);
            Pair<EnumFacing, AxisAlignedBB> result = rayTraceBoxes(playerIn, actualState, pos);
            EnumFacing ioSide = result.key;
            if(ioSide != null)
            {
                //Change IO
                ECableIO io = te.getSideIO(ioSide);
                te.setSideIO(ioSide, io.next());
                worldIn.notifyBlockUpdate(pos, actualState, getActualState(state, worldIn, pos), 3);
            }
        }
        return true;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(world, pos, state);
        if(!world.isRemote)
            NetworkData.addNewComponent(world, pos);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosionIn)
    {
        super.onBlockDestroyedByExplosion(world, pos, explosionIn);
        if(!world.isRemote)
            NetworkData.onCableRemoved(world, pos);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        super.onBlockHarvested(world, pos, state, player);
        if(!world.isRemote)
            NetworkData.onCableRemoved(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbour)
    {
        //TODO: This is only ever called on the server side. Do I need to send a packet to the client for TileCable#determineSideIO?
        Sparkz.logger.info("Neighbour change -> {}", pos);
        IBlockState actualBefore = getActualState(state, world, pos);
        TileCable cableTE = getTileEntity(world, pos);
        cableTE.determineSideIO(world, neighbour);

        //Try to add or remove the neighbour as IO in the network
        if(world.isAirBlock(neighbour))
        {
            boolean removed = cableTE.getNetwork().removeIO(neighbour);
            if(removed) Sparkz.logger.info("Removed IO {} from network {}", neighbour, cableTE.getNetwork());
        }
        else
        {
            IEnergy energy = IEnergy.create(world, neighbour, null);
            EnumFacing side = CommonUtils.getSide(pos, neighbour);
            if(energy != null)
            {
                if(energy.canInput(side)) cableTE.getNetwork().addConsumer(neighbour);
                if(energy.canOutput(side)) cableTE.getNetwork().addProducer(neighbour);
            }
        }
        world.notifyBlockUpdate(pos, actualBefore, getActualState(state, world, pos), 3);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState)
    {
        if(!isActualState)
            state = state.getActualState(world, pos);

        //addCollisionBoxToList(pos, entityBox, collidingBoxes, CENTER_BOX);
        getBoundingBoxesForState(state).forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        RayTraceResult closestRay = null;
        double closestDist = Double.MAX_VALUE;

        for(AxisAlignedBB box : getBoundingBoxesForState(getActualState(blockState, worldIn, pos)))
        {
            RayTraceResult ray = rayTrace(pos, start, end, box);
            if(ray != null)
            {
                double rayDist = ray.hitVec.squareDistanceTo(start);
                if(rayDist < closestDist)
                {
                    closestRay = ray;
                    closestDist = rayDist;
                }
            }
        }
        return closestRay;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        IBlockState actualState = getActualState(state, source, pos);
        double minX = CENTER_BOX.minX;
        double minY = CENTER_BOX.minY;
        double minZ = CENTER_BOX.minZ;
        double maxX = CENTER_BOX.maxX;
        double maxY = CENTER_BOX.maxY;
        double maxZ = CENTER_BOX.maxZ;
        for(AxisAlignedBB box : getBoundingBoxesForState(actualState))
        {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        if(!worldIn.isRemote)
            return super.getSelectedBoundingBox(state, worldIn, pos);

        //Calculate start and end pos
        //Using ForgeHooks.rayTraceEyes as reference
        EntityPlayer player = Minecraft.getMinecraft().player;
        Vec3d startPos = player.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks());
        double dist = startPos.distanceTo(new Vec3d(pos));
        Vec3d endPos = startPos.add(player.getLookVec().scale(dist + 1.5D));

        AxisAlignedBB closestBox = null;
        double closestDist = Double.MAX_VALUE;

        for(AxisAlignedBB box : getBoundingBoxesForState(getActualState(state, worldIn, pos)))
        {
            RayTraceResult ray = rayTrace(pos, startPos, endPos, box);
            if(ray != null)
            {
                double rayDist = ray.hitVec.squareDistanceTo(startPos);
                if(rayDist < closestDist)
                {
                    closestBox = box;
                    closestDist = rayDist;
                }
            }
        }

        return closestBox == null ? null : closestBox.offset(pos);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileCable te = getTileEntity(world, pos);
        if(te == null)
            return state;
        for(EnumFacing facing : EnumFacing.VALUES)
            state = state.withProperty(IO_PROPERTIES.get(facing.getIndex()), te.getSideIO(facing));
        return state;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, IO_PROPERTIES.toArray(new IProperty[0]));
    }
}
