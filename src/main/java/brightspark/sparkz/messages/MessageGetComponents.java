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

public class MessageGetComponents implements IMessage
{
    public List<BlockPos> cables, inputs, outputs;

    public MessageGetComponents() {}

    public MessageGetComponents(List<BlockPos> cables, List<BlockPos> inputs, List<BlockPos> outputs)
    {
        this.cables = cables;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int cablesSize = buf.readInt();
        int inputsSize = buf.readInt();
        int outputsSize = buf.readInt();
        cables = new ArrayList<>(cablesSize);
        for(int i = 0; i < cablesSize; i++)
            cables.add(BlockPos.fromLong(buf.readLong()));
        inputs = new ArrayList<>(inputsSize);
        for(int i = 0; i < inputsSize; i++)
            inputs.add(BlockPos.fromLong(buf.readLong()));
        outputs = new ArrayList<>(outputsSize);
        for(int i = 0; i < outputsSize; i++)
            outputs.add(BlockPos.fromLong(buf.readLong()));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(cables.size());
        buf.writeInt(inputs.size());
        buf.writeInt(outputs.size());
        cables.forEach((cable) -> buf.writeLong(cable.toLong()));
        inputs.forEach((input) -> buf.writeLong(input.toLong()));
        outputs.forEach((output) -> buf.writeLong(output.toLong()));
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
                SItems.debug.inputsToHighlight = message.inputs;
                SItems.debug.outputsToHighlight = message.outputs;
            });
            return null;
        }
    }
}
