package brightspark.sparkz;

import brightspark.sparkz.energy.NetworkData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
public class EventHandler
{
    @SubscribeEvent
    public static void tickNetworks(TickEvent.WorldTickEvent event)
    {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.START)
            NetworkData.tickNetworks(event.world);
    }
}
