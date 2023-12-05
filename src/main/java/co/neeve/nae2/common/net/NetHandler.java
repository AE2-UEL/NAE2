package co.neeve.nae2.common.net;

import co.neeve.nae2.Tags;
import co.neeve.nae2.common.net.messages.PatternMultiToolPacket;
import co.neeve.nae2.common.net.messages.ReconstructorFXPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetHandler implements IMessageHandler<INAEMessage, INAEMessage> {

	private final SimpleNetworkWrapper channel;

	public NetHandler() {
		this.channel = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

		this.channel.registerMessage(this, PatternMultiToolPacket.class, 0, Side.SERVER);
		this.channel.registerMessage(this, ReconstructorFXPacket.class, 1, Side.CLIENT);
	}

	@Override
	public INAEMessage onMessage(INAEMessage message, MessageContext ctx) {
		Runnable runnable = () -> message.process(ctx);

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			var player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(runnable);
		} else {
			Minecraft.getMinecraft().addScheduledTask(runnable);
		}

		return null;
	}

	public SimpleNetworkWrapper getChannel() {
		return this.channel;
	}
}
