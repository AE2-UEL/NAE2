package co.neeve.nae2.common.parts;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public abstract class NAEBasePartState extends AEBasePart implements IPowerChannelState {
	protected static final int POWERED_FLAG = 1;
	private int clientFlags = 0; // sent as byte.

	public NAEBasePartState(ItemStack is) {
		super(is);
	}

	@MENetworkEventSubscribe
	public void chanRender(final MENetworkChannelsChanged c) {
		this.getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender(final MENetworkPowerStatusChange c) {
		this.getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void bootingRender(final MENetworkBootingStatusChange bs) {
		this.getHost().markForUpdate();
	}

	@Override
	public void writeToStream(final ByteBuf data) throws IOException {
		super.writeToStream(data);

		this.setClientFlags(0);

		try {
			if (this.getProxy().getEnergy().isNetworkPowered()) {
				this.setClientFlags(this.getClientFlags() | POWERED_FLAG);
			}

			this.setClientFlags(this.populateFlags(this.getClientFlags()));
		} catch (final GridAccessException e) {
			// meh
		}

		data.writeByte((byte) this.getClientFlags());
	}

	protected int populateFlags(final int cf) {
		return cf;
	}

	@Override
	public boolean readFromStream(final ByteBuf data) throws IOException {
		final var eh = super.readFromStream(data);

		final var old = this.getClientFlags();
		this.setClientFlags(data.readByte());

		return eh || old != this.getClientFlags();
	}

	@Override
	public boolean isPowered() {
		return (this.getClientFlags() & POWERED_FLAG) == POWERED_FLAG;
	}

	@Override
	public boolean isActive() {
		return this.isPowered();
	}

	public int getClientFlags() {
		return this.clientFlags;
	}

	private void setClientFlags(final int clientFlags) {
		this.clientFlags = clientFlags;
	}
}
