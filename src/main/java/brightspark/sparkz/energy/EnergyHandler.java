package brightspark.sparkz.energy;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber
public class EnergyHandler
{
    private static List<EnergyNetwork> networks = new ArrayList<>();

    private static EnergyNetwork getNetworkWithCable(BlockPos cablePos)
    {
        for(EnergyNetwork network : networks)
            if(network.hasCablePos(cablePos))
                return network;
        return null;
    }

    public static void addToEnergyNetwork(World world, BlockPos cable)
    {
        //TODO
    }

    public static void newEnergyNetwork(World world, BlockPos cable)
    {
        networks.add(new EnergyNetwork(world, cable));
    }

    public static void onCableDestroyed(IBlockAccess world, BlockPos cablePos)
    {
        //TODO: Check networks at cable position
        //Gets the network affected
        EnergyNetwork affectedNetwork = getNetworkWithCable(cablePos);
    }

    @SubscribeEvent
    public static void tickNetworks(TickEvent.WorldTickEvent event)
    {
        if(event.side != Side.SERVER || event.phase != TickEvent.Phase.START)
            return;

        //Tick networks
        World world = event.world;
        Iterator<EnergyNetwork> iterator = networks.iterator();
        while(iterator.hasNext())
        {
            EnergyNetwork network = iterator.next();
            if(network.hasCables())
                network.update(world);
            else
                iterator.remove();
        }
    }
}
