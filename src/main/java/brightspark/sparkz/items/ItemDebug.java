package brightspark.sparkz.items;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.energy.EnergyNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemDebug extends Item
{
    public ItemDebug()
    {
        setUnlocalizedName("debug");
        setRegistryName("debug");
        setCreativeTab(Sparkz.TAB);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        if(world.isRemote) return EnumActionResult.SUCCESS;
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileCable)
        {
            TileCable cable = (TileCable) te;
            EnergyNetwork network = cable.getNetwork();
            if(network != null)
                player.sendMessage(new TextComponentString(
                        "Cable at " + pos +
                                "\nPart of network: " + network +
                                "\nNetwork has " + network.getNumCables() + " cables, " + network.getNumInputs() + " inputs and " + network.getNumOutputs() + " outputs"
                ));
            else
                player.sendMessage(new TextComponentString(
                        "Cable at " + pos +
                                "\nHas no network"
                ));
        }
        return EnumActionResult.SUCCESS;
    }
}
