package brightspark.sparkz.energy;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class EnergyBlock
{
    public IEnergy energy;
    public BlockPos pos;
    /** Side of this block which has a cable */
    public EnumFacing side;

    public EnergyBlock(IEnergy energy, BlockPos pos, EnumFacing side)
    {
        this.energy = energy;
        this.pos = pos;
        this.side = side;
    }
}
