package brightspark.sparkz.messages;

import brightspark.sparkz.init.SItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class MessageGetCables implements IMessage
{
    public List<BlockPos> cables;

    public MessageGetCables() {}

    public MessageGetCables(List<BlockPos> cables)
    {
        this.cables = cables;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int size = buf.readInt();
        cables = new ArrayList<>(size);
        for(int i = 0; i < size; i++)
            cables.add(BlockPos.fromLong(buf.readLong()));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(cables.size());
        cables.forEach((cable) -> buf.writeLong(cable.toLong()));
    }

    public static class Handler implements IMessageHandler<MessageGetCables, IMessage>
    {
        @Override
        public IMessage onMessage(MessageGetCables message, MessageContext ctx)
        {
            final IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(() -> SItems.debug.setCablesToHighlight(message.cables));
            return null;
        }
    }
}
