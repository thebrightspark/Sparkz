package brightspark.sparkz.blocks;

import brightspark.sparkz.energy.IEnergy;
import brightspark.sparkz.energy.NetworkHandler;
import brightspark.sparkz.util.CommonUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockCable extends AbstractBlockContainer<TileCable>
{
    //https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/2face865be2516c59af77d2b93fb98bc22bbd71e/src/main/java/com/choonster/testmod3/block/pipe/BlockPipeBase.java
    //http://www.minecraftforge.net/forum/topic/34236-18-pipes/
    public static final AxisAlignedBB CENTER_BOX = new AxisAlignedBB(0.375f, 0.375f, 0.375f, 0.625f, 0.625f, 0.625f);
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
        CONNECTED_PROPERTIES.forEach((prop) -> defaultState.withProperty(prop, false));
        setDefaultState(defaultState);
    }

    private boolean isConnected(IBlockState state, EnumFacing facing)
    {
        return state.getValue(CONNECTED_PROPERTIES.get(facing.getIndex()));
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        return CommonUtils.canCableConnect(world, pos.offset(facing));
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
        NetworkHandler.addNewComponent(world, pos);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosionIn)
    {
        super.onBlockDestroyedByExplosion(world, pos, explosionIn);
        NetworkHandler.onCableRemoved(world, pos);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        super.onBlockHarvested(world, pos, state, player);
        NetworkHandler.onCableRemoved(world, pos);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        super.onNeighborChange(world, pos, neighbor);
        TileCable cableTE = getTileEntity(world, pos);
        cableTE.determineSideIO(world, neighbor);
        IEnergy energy = IEnergy.create(world, neighbor, null);
        if(energy != null)
        {
            if(energy.canInput())   cableTE.getNetwork().addInput(neighbor);
            if(energy.canOutput())  cableTE.getNetwork().addOutput(neighbor);
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

        addCollisionBoxToList(pos, entityBox, collidingBoxes, CENTER_BOX);

        for(EnumFacing facing : EnumFacing.VALUES)
        {
            if(isConnected(state, facing))
            {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, CONNECTED_BOXES.get(facing.getIndex()));
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        return super.getSelectedBoundingBox(state, worldIn, pos);
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
        return new BlockStateContainer(this, CONNECTED_PROPERTIES.toArray(new PropertyBool[CONNECTED_PROPERTIES.size()]));
    }
}
