package brightspark.sparkz.messages;

import brightspark.sparkz.init.SItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashSet;
import java.util.Set;

public class MessageGetComponents implements IMessage
{
    public Set<BlockPos> cables, consumers, producers;

    public MessageGetComponents() {}

    public MessageGetComponents(Set<BlockPos> cables, Set<BlockPos> consumers, Set<BlockPos> producers)
    {
        this.cables = cables;
        this.consumers = consumers;
        this.producers = producers;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int cablesSize = buf.readInt();
        int inputsSize = buf.readInt();
        int outputsSize = buf.readInt();
        cables = new HashSet<>(cablesSize);
        for(int i = 0; i < cablesSize; i++)
            cables.add(BlockPos.fromLong(buf.readLong()));
        consumers = new HashSet<>(inputsSize);
        for(int i = 0; i < inputsSize; i++)
            consumers.add(BlockPos.fromLong(buf.readLong()));
        producers = new HashSet<>(outputsSize);
        for(int i = 0; i < outputsSize; i++)
            producers.add(BlockPos.fromLong(buf.readLong()));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(cables.size());
        buf.writeInt(consumers.size());
        buf.writeInt(producers.size());
        cables.forEach((cable) -> buf.writeLong(cable.toLong()));
        consumers.forEach((input) -> buf.writeLong(input.toLong()));
        producers.forEach((output) -> buf.writeLong(output.toLong()));
    }

    public static class Handler implements IMessageHandler<MessageGetComponents, IMessage>
    {
        @Override
        public IMessage onMessage(MessageGetComponents message, MessageContext ctx)
        {
            final IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(() ->
            {
                SItems.debug.cablesToHighlight = message.cables;
                SItems.debug.consumersToHighlight = message.consumers;
                SItems.debug.producersToHighlight = message.producers;
            });
            return null;
        }
    }
}
