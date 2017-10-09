package brightspark.sparkz.energy;

import brightspark.sparkz.blocks.TileCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber
public class EnergyHandler
{
    //TODO: Need to think about how this will all work with dimensions!

    private static List<EnergyNetwork> networks = new ArrayList<>();
    private static ScheduledExecutorService threadScheduler = new ScheduledThreadPoolExecutor(1);

    private static void runInThread(Runnable runnable)
    {
        threadScheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    private static EnergyNetwork getNetworkWithCable(BlockPos cablePos)
    {
        for(EnergyNetwork network : networks)
            if(network.hasCablePos(cablePos))
                return network;
        return null;
    }

    public static void addToEnergyNetwork(World world, BlockPos componentPos)
    {
        if(world.isRemote) return;
        runInThread(() ->
        {
            for(EnergyNetwork network : networks)
                if(network.canAddComponent(componentPos))
                {
                    TileEntity te = world.getTileEntity(componentPos);
                    if(te == null) continue;
                    if(te instanceof TileCable)
                        network.addCable(world, componentPos);
                    else
                    {
                        IEnergy energy = IEnergy.create(te, null);
                        if(energy == null)
                            continue;
                        if(energy.canOutput())
                            network.addInput(componentPos);
                        else if(energy.canInput())
                            network.addOutput(componentPos);

                    }
                    break;
                }
        });
    }

    public static void removeNetwork(EnergyNetwork network)
    {
        networks.remove(network);
    }

    public static void newEnergyNetwork(World world, BlockPos cable)
    {
        if(world.isRemote) return;
        networks.add(new EnergyNetwork(world, cable));
    }

    public static void onCableRemoved(World world, BlockPos cablePos)
    {
        //TODO: Check networks at cable position
        if(world.isRemote) return;
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
