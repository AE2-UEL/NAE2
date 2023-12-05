package co.neeve.nae2.common.net.messages;

import co.neeve.nae2.common.net.INAEMessage;
import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ReconstructorFXPacket implements INAEMessage {
	private BlockPos pos;

	public ReconstructorFXPacket() {}

	public ReconstructorFXPacket(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.pos.getX());
		buf.writeInt(this.pos.getY());
		buf.writeInt(this.pos.getZ());
	}

	@Override
	public void process(MessageContext ctx) {
		var te = Minecraft.getMinecraft().world.getTileEntity(this.pos);
		if (te instanceof TileReconstructionChamber trc) {
			trc.spawnHologram();
		}
	}
}
