package co.neeve.nae2.items.patternmultitool.net;

import co.neeve.nae2.common.enums.PatternMultiToolActionTypes;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PatternMultiToolPacket implements IMessage {
	private int value;
	private int actionType;

	public PatternMultiToolPacket() {
	}

	public PatternMultiToolPacket(PatternMultiToolActionTypes actionType, int value) {
		this.actionType = actionType.ordinal();
		this.value = value;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(actionType);
		buf.writeInt(value);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		actionType = buf.readInt();
		value = buf.readInt();
	}

	public int getValue() {
		return this.value;
	}

	public PatternMultiToolActionTypes getActionType() {
		return PatternMultiToolActionTypes.values()[this.actionType];
	}
}
