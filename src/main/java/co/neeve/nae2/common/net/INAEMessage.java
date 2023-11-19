package co.neeve.nae2.common.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface INAEMessage extends IMessage {
	void process(MessageContext ctx);
}
