package co.neeve.nae2.items.patternmultitool.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PatternMultiToolPacket implements IMessage {
	private int buttonId;

	public PatternMultiToolPacket() {
	}

	public PatternMultiToolPacket(int buttonId) {
		this.buttonId = buttonId;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(buttonId);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		buttonId = buf.readInt();
	}

	public int getButtonId() {
		return buttonId;
	}
}
