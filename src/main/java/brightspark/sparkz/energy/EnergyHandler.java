package brightspark.sparkz.energy;

import brightspark.sparkz.Sparkz;
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
            //Add to existing adjacent network if there is one
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
                    Sparkz.logger.info("Added block %s at %s to energy network %s",
                            world.getBlockState(componentPos).getBlock().getRegistryName(), componentPos, network);
                    break;
                }
            //No network found adjacent to block placed - Create new network
            EnergyNetwork network = newEnergyNetwork(world, componentPos);
            Sparkz.logger.info("Added block %s at %s to NEW energy network %s",
                    world.getBlockState(componentPos).getBlock().getRegistryName(), componentPos, network);
        });
    }

    public static void removeNetwork(EnergyNetwork network)
    {
        Sparkz.logger.info("Removing network %s", network);
        networks.remove(network);
    }

    public static EnergyNetwork newEnergyNetwork(World world, BlockPos cable)
    {
        if(world.isRemote) return null;
        EnergyNetwork network = new EnergyNetwork(cable);
        networks.add(network);
        Sparkz.logger.info("Created new network %s", network);
        return network;
    }

    public static void onCableRemoved(World world, BlockPos cablePos)
    {
        if(world.isRemote) return;
        onCableRemoved(world, cablePos, getNetworkWithCable(cablePos));
    }

    public static void onCableRemoved(World world, BlockPos cablePos, EnergyNetwork network)
    {
        if(world.isRemote || network == null) return;
        Sparkz.logger.info("Cable at %s removed from network %s", cablePos, network);
        network.removeCable(cablePos);
        if(network.hasCables())
        {
            Sparkz.logger.info("Splitting network %s at position %s", network, cablePos);
            runInThread(() ->
            {
                List<EnergyNetwork> newNetworks = network.splitAt(cablePos);
                if(newNetworks.size() > 0)
                {
                    Sparkz.logger.info("Adding %s new networks due to split of network %s",
                            newNetworks.size(), network);
                    networks.addAll(newNetworks);
                }
            });
        }
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
