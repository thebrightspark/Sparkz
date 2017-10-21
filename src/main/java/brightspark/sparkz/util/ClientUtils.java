package brightspark.sparkz.util;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientUtils
{
    private static Minecraft mc = Minecraft.getMinecraft();

    public static ItemStack getHeldItem(Item item)
    {
        for(ItemStack held : mc.player.getHeldEquipment())
            if(held != null && held.getItem().equals(item))
                return held;
        return null;
    }
}
