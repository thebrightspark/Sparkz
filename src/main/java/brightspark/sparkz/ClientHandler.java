package brightspark.sparkz;

import brightspark.sparkz.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Set;

@Mod.EventBusSubscriber
public class ClientHandler
{
    private static Minecraft mc = Minecraft.getMinecraft();
    private static Color cableColour = Color.WHITE;
    private static Color consumerColour = Color.BLUE;
    private static Color producerColour = Color.ORANGE;

    private static void renderBox(BlockPos pos, double partialTicks, Color colour)
    {
        renderBox(new AxisAlignedBB(pos).grow(0.001d), partialTicks, colour);
    }

    private static void renderBox(AxisAlignedBB box, double partialTicks, Color colour)
    {
        //Get player's actual position
        EntityPlayerSP player = mc.player;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        //Render the box
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(5f);
        GlStateManager.disableTexture2D();
        GlStateManager.translate(-x, -y, -z);
        float[] rgb = colour.getRGBColorComponents(new float[3]);
        RenderGlobal.renderFilledBox(box, rgb[0], rgb[1], rgb[2], 0.2f);
        RenderGlobal.drawSelectionBoundingBox(box, rgb[0], rgb[1], rgb[2], 0.4f);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void highlightNetwork(RenderWorldLastEvent event)
    {
        ItemStack heldItem = ClientUtils.getHeldItem(Sparkz.debug);
        if(heldItem == null) return;

        Set<BlockPos> cables = Sparkz.debug.cablesToHighlight;
        if(cables != null)
            for(BlockPos pos : cables)
                renderBox(pos, event.getPartialTicks(), cableColour);

        Set<BlockPos> inputs = Sparkz.debug.consumersToHighlight;
        if(inputs != null)
            for(BlockPos pos : inputs)
                renderBox(pos, event.getPartialTicks(), consumerColour);

        Set<BlockPos> outputs = Sparkz.debug.producersToHighlight;
        if(outputs != null)
            for(BlockPos pos : outputs)
                renderBox(pos, event.getPartialTicks(), producerColour);
    }
}
