package brightspark.sparkz.util;

import brightspark.sparkz.blocks.BlockCable;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.energy.EnergyNetwork;
import brightspark.sparkz.energy.IEnergy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class CommonUtils
{
    /**
     * Returns whether the two block positions are adjacent to each other
     */
    public static boolean areBlocksAdjacent(BlockPos pos1, BlockPos pos2)
    {
        int xDif = Math.abs(pos1.getX() - pos2.getX());
        int yDif = Math.abs(pos1.getY() - pos2.getY());
        int zDif = Math.abs(pos1.getZ() - pos2.getZ());
        boolean result = xDif + yDif + zDif == 1;
        //Sparkz.logger.info("Are adjacent? {} -> Pos1: {}, Pos2: {}", result, pos1, pos2);
        return result;
    }

    /**
     * Returns the side that pos2 is on relative to pos1
     */
    public static EnumFacing getSide(BlockPos pos1, BlockPos pos2)
    {
        int vecX = Integer.compare(pos2.getX(), pos1.getX());
        int vecY = Integer.compare(pos2.getY(), pos1.getY());
        int vecZ = Integer.compare(pos2.getZ(), pos1.getZ());
        return EnumFacing.getFacingFromVector(vecX, vecY, vecZ);
    }

    /**
     * Returns a side which has a cable in this network
     */
    public static EnumFacing getSideWithCable(World world, BlockPos pos, EnergyNetwork network)
    {
        for(EnumFacing side : EnumFacing.VALUES)
        {
            BlockPos sidePos = pos.offset(side);
            if(isCable(world, sidePos) && network.hasCablePos(sidePos))
                return side;
        }
        return null;
    }

    /**
     * Returns whether cable can be connected to the block at the given position
     */
    public static boolean canCableConnect(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return isCable(world, pos) || isIO(world, pos, side);
    }

    /**
     * Returns the amount of adjacent cables to this position
     */
    public static int countAdjacentCables(IBlockAccess world, BlockPos pos)
    {
        int num = 0;
        for(EnumFacing side : EnumFacing.values())
            if(isCable(world, pos.offset(side)))
                num++;
        return num;
    }

    /**
     * Returns true if the block at the position is a cable
     */
    public static boolean isCable(IBlockAccess world, BlockPos pos)
    {
        return world.getBlockState(pos).getBlock() instanceof BlockCable;
    }

    /**
     * Returns true if the block at the position can accept or output energy
     */
    public static boolean isIO(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        IEnergy energy = IEnergy.create(world, pos, side);
        return energy != null && (energy.canInput(side) || energy.canOutput(side));
    }

    /**
     * Finds an adjacent network
     */
    public static Set<EnergyNetwork> findAdjacentNetworks(IBlockAccess world, BlockPos pos)
    {
        Set<EnergyNetwork> adjacentNetworks = new HashSet<>();
        for(EnumFacing side : EnumFacing.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(side));
            if(te instanceof TileCable)
            {
                EnergyNetwork otherNetwork = ((TileCable) te).getNetwork();
                adjacentNetworks.add(otherNetwork);
            }
        }
        return adjacentNetworks;
    }

    public static Set<Set<BlockPos>> getAllAdjacentConnectedCables(IBlockAccess world, BlockPos pos)
    {
        Set<Set<BlockPos>> networks = new HashSet<>();
        for(EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos nextPos = pos.offset(facing);
            if(!isCable(world, nextPos))
                continue;
            boolean alreadyInNetwork = false;
            for(Set<BlockPos> network : networks)
            {
                if(network.contains(nextPos))
                {
                    alreadyInNetwork = true;
                    break;
                }
            }
            if(alreadyInNetwork)
                continue;
            Set<BlockPos> connectedCables = getAllAdjacentConnectedCables(world, new HashSet<>(), nextPos);
            connectedCables.add(nextPos);
            if(!connectedCables.isEmpty())
                networks.add(connectedCables);
        }
        return networks;
    }

    public static Set<BlockPos> getAllConnectedCables(IBlockAccess world, BlockPos pos)
    {
        Set<BlockPos> cables = new HashSet<>();
        if(!isCable(world, pos)) return cables;
        cables.add(pos);
        return getAllAdjacentConnectedCables(world, cables, pos);
    }

    private static Set<BlockPos> getAllAdjacentConnectedCables(IBlockAccess world, Set<BlockPos> cables, BlockPos pos)
    {
        for(EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos nextPos = pos.offset(facing);
            if(isCable(world, nextPos) && !cables.contains(nextPos))
            {
                cables.add(nextPos);
                getAllAdjacentConnectedCables(world, cables, nextPos);
            }
        }
        return cables;
    }
}
