package brightspark.sparkz.items;

import brightspark.sparkz.Sparkz;
import brightspark.sparkz.blocks.TileCable;
import brightspark.sparkz.energy.EnergyNetwork;
import brightspark.sparkz.messages.MessageGetComponents;
import brightspark.sparkz.util.CommonUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.Set;

public class ItemDebug extends Item
{
    public Set<BlockPos> cablesToHighlight = null;
    public Set<BlockPos> consumersToHighlight = null;
    public Set<BlockPos> producersToHighlight = null;

    public ItemDebug()
    {
        setUnlocalizedName("debug");
        setRegistryName("debug");
        setCreativeTab(Sparkz.TAB);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if(player.isSneaking())
        {
            if(world.isRemote)
            {
                cablesToHighlight = null;
                consumersToHighlight = null;
                producersToHighlight = null;
                player.sendMessage(new TextComponentString("Cleared saved network"));
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        if(world.isRemote) return EnumActionResult.PASS;
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileCable)
        {
            TileCable cable = (TileCable) te;
            EnergyNetwork network = cable.getNetwork();
            if(network != null)
            {
                network.checkNetwork();
                Set<BlockPos> cables, consumers, producers;
                if(player.isSneaking())
                    cables = CommonUtils.getAllConnectedCables(world, pos);
                else
                    cables = network.getCables();
                consumers = network.getConsumers();
                producers = network.getProducers();
                Sparkz.network.sendTo(new MessageGetComponents(cables, consumers, producers), (EntityPlayerMP) player);
                player.sendMessage(new TextComponentString(
                        "Cable at " + pos +
                                "\nPart of network: " + network +
                                "\nNetwork has " + network.getNumCables() + " cables, " + network.getNumConsumers() + " consumers and " + network.getNumProducers() + " producers"));
            }
            else
            {
                player.sendMessage(new TextComponentString(
                        "Cable at " + pos +
                                "\nHas no network"
                ));
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
