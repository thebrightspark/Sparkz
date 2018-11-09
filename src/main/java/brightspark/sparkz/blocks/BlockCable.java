package brightspark.sparkz.blocks;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.energy.IEnergy;
import brightspark.sparkz.energy.NetworkData;
import brightspark.sparkz.util.CommonUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
    public static final ImmutableList<PropertyBool> CONNECTED_PROPERTIES = ImmutableList.copyOf(
            Stream.of(EnumFacing.VALUES)
                    .map(facing -> PropertyBool.create(facing.getName()))
                    .collect(Collectors.toList()));

    public BlockCable()
    {
        super("cable");
        IBlockState defaultState = blockState.getBaseState();
        for(PropertyBool prop : CONNECTED_PROPERTIES)
            defaultState = defaultState.withProperty(prop, false);
        setDefaultState(defaultState);
    }

    private boolean isConnected(IBlockState state, EnumFacing facing)
    {
        return state.getValue(CONNECTED_PROPERTIES.get(facing.getIndex()));
    }

    private AxisAlignedBB getConnectionBox(EnumFacing facing)
    {
        return CONNECTED_BOXES.get(facing.getIndex());
    }

    /**
     * Gets all bounding boxes for current connections
     */
    private Set<AxisAlignedBB> getConnectionBoxes(IBlockState state)
    {
        Set<AxisAlignedBB> boxes = Arrays.stream(EnumFacing.VALUES).filter(facing -> isConnected(state, facing)).map(this::getConnectionBox).collect(Collectors.toSet());
        boxes.add(CENTER_BOX);
        return boxes;
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        return CommonUtils.canCableConnect(world, pos.offset(facing), facing.getOpposite());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileCable();
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
        Sparkz.logger.info("Neighbour change!");
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
        getConnectionBoxes(state).forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        RayTraceResult closestRay = null;
        double closestDist = Double.MAX_VALUE;

        for(AxisAlignedBB box : getConnectionBoxes(getActualState(blockState, worldIn, pos)))
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
        for(AxisAlignedBB box : getConnectionBoxes(actualState))
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

        for(AxisAlignedBB box : getConnectionBoxes(getActualState(state, worldIn, pos)))
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
        for(EnumFacing facing : EnumFacing.VALUES)
            state = state.withProperty(CONNECTED_PROPERTIES.get(facing.getIndex()), canBeConnectedTo(world, pos, facing));
        return state;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, CONNECTED_PROPERTIES.toArray(new PropertyBool[0]));
    }
}
