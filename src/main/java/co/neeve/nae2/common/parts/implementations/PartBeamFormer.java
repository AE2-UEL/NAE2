package co.neeve.nae2.common.parts.implementations;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AELog;
import appeng.me.GridAccessException;
import appeng.parts.ICableBusContainer;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import co.neeve.nae2.common.parts.NAEBasePartState;
import co.neeve.nae2.server.IBlockStateListener;
import co.neeve.nae2.server.WorldListener;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;

import static appeng.parts.automation.PartLevelEmitter.MODEL_OFF_OFF;
import static appeng.parts.automation.PartLevelEmitter.MODEL_ON_HAS_CHANNEL;

public class PartBeamFormer extends NAEBasePartState implements IBlockStateListener, IGridTickable {
	private static final ResourceLocation END_GATEWAY_BEAM_TEXTURE = new ResourceLocation("textures/entity" +
		"/end_gateway_beam.png");
	private double beamLength = 0;
	private PartBeamFormer otherBeamFormer = null;
	private IGridConnection connection = null;
	private Long2ObjectLinkedOpenHashMap<BlockPos> listenerLinkedList = null;
	private double oldBeamLength = 0;
	private BlockPos breakPos = null;

	public PartBeamFormer(ItemStack is) {
		super(is);
		this.getProxy().setFlags(GridFlags.PREFERRED);
	}

	public @NotNull IPartModel getStaticModels() {
		return !(this.isActive() && this.isPowered()) ? MODEL_OFF_OFF : MODEL_ON_HAS_CHANNEL;
	}

	@Override
	public void getBoxes(final IPartCollisionHelper bch) {
		bch.addBox(7, 7, 11, 9, 9, 16);
	}

	@Override
	public float getCableConnectionLength(AECableType cable) {
		return 16;
	}

	@Override
	public @NotNull AECableType getCableConnectionType(final @NotNull AEPartLocation dir) {
		return AECableType.SMART;
	}

	@Override
	public void removeFromWorld() {
		this.unregisterListener();
		this.disconnect();
		this.getProxy().invalidate();
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
	}

	@Override
	public void setPartHostInfo(AEPartLocation side, IPartHost host, TileEntity tile) {
		super.setPartHostInfo(side, host, tile);
	}

	@MENetworkEventSubscribe
	public void onPower(MENetworkPowerStatusChange event) throws GridAccessException {
		if (!this.getProxy().isReady()) return;
		this.getProxy().getTick().alertDevice(this.getGridNode());
	}

	@MENetworkEventSubscribe
	public void onUpdate(MENetworkBootingStatusChange event) throws GridAccessException {
		if (!this.getProxy().isReady()) return;
		this.getProxy().getTick().alertDevice(this.getGridNode());
	}

	private void unregisterListener() {
		WorldListener.instance.unregisterBlockStateListener(this);
	}

	private void recalcListener(HashSet<BlockPos> locs) {
		this.unregisterListener();
		listenerLinkedList = new Long2ObjectLinkedOpenHashMap<>();
		for (var loc : locs) {
			listenerLinkedList.put(loc.toLong(), loc);
		}

		var otherpos = otherBeamFormer.getTile().getPos();
		this.beamLength = this.getTile().getPos().getDistance(otherpos.getX(), otherpos.getY(), otherpos.getZ()) - 1;
		this.otherBeamFormer.beamLength = 0;

		WorldListener.instance.registerBlockStateListener(this, locs);
	}


	@Override
	public void renderDynamic(double x, double y, double z, float partialTicks, int destroyStage) {
		if (this.beamLength != 0 && this.isActive() && this.isPowered()) {
			var color = this.getHost().getColor();
			var scale = 255;
			float[] rgb = new float[]{ ((color.mediumVariant >> 16) & 0xff) * scale,
				((color.mediumVariant >> 8) & 0xff) * scale,
				(color.mediumVariant & 0xff) * scale };

			// Get the direction vector
			var facing = this.getSide().getFacing();
			int dx = facing.getXOffset();
			int dy = facing.getYOffset();
			int dz = facing.getZOffset();

			// Calculate the pitch, yaw, and roll based on the direction vector
			float pitch = (float) Math.atan2(Math.sqrt(dx * dx + dz * dz), dy) * (180F / (float) Math.PI);
			float yaw = (float) (180 - Math.atan2(dz, dx) * (180F / (float) Math.PI) - 90.0F);

			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.pushMatrix();

			// Translate and rotate
			GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
			GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(-0.5, 0.5, -0.5);

			Minecraft.getMinecraft().renderEngine.bindTexture(END_GATEWAY_BEAM_TEXTURE);
			TileEntityBeaconRenderer.renderBeamSegment(0, 0, 0, partialTicks, 1,
				(double) this.getHost().getTile().getWorld().getTotalWorldTime(), 0, (int) Math.floor(beamLength)
				, rgb, 0.075, 0.1);

			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean requireDynamicRender() {
		if (this.getHost() instanceof ICableBusContainer bc && bc instanceof IBeamFormerHost beamFormerHost) {
			beamFormerHost.notifyBeamFormerState();
		}
		return true;
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		var shouldRedraw = super.readFromStream(data);

		this.beamLength = data.readDouble();
		shouldRedraw |= this.oldBeamLength != this.beamLength;
		this.oldBeamLength = this.beamLength;

		return shouldRedraw;
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		data.writeDouble(this.beamLength);
	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		try {
			var isValid = !newState.getMaterial().isOpaque();
			if (connection != null && !isValid) {
				breakPos = pos;
				this.getProxy().getTick().alertDevice(this.getGridNode());
			} else if (connection == null && isValid) {
				this.getProxy().getTick().alertDevice(this.getGridNode());
			}
		} catch (GridAccessException ignored) {}
	}

	public void disconnect() {
		if (this.connection == null) return;

		long newBeamA = 0;
		long newBeamB = 0;

		if (this.breakPos != null) {
			newBeamA += 0.25;
			newBeamB += 0.25;
			var iterator = this.listenerLinkedList.long2ObjectEntrySet().fastIterator();
			var hash = this.breakPos.toLong();
			while (iterator.hasNext()) {
				if (iterator.next().getLongKey() == hash) break;
				newBeamA++;
			}

			while (iterator.hasNext()) {
				iterator.next();
				newBeamB++;
			}

			breakPos = null;
		}

		this.beamLength = newBeamA;
		this.connection.destroy();
		this.connection = null;
		this.getHost().markForUpdate();

		if (this.otherBeamFormer != null && this.otherBeamFormer.otherBeamFormer == this) {
			this.otherBeamFormer.beamLength = newBeamB;
			this.otherBeamFormer.connection = null;
			this.otherBeamFormer.otherBeamFormer = null;
			this.otherBeamFormer.getHost().markForUpdate();
			this.otherBeamFormer = null;
		}
	}

	@Override
	public boolean onPartShiftActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		super.onPartShiftActivate(player, hand, pos);

		try {
			this.getProxy().getTick().alertDevice(this.getGridNode());
		} catch (GridAccessException ignored) {
		}
		return true;
	}

	@NotNull
	@Override
	public TickingRequest getTickingRequest(@NotNull IGridNode node) {
		return new TickingRequest(20, 300, false, true);
	}

	@NotNull
	@Override
	public TickRateModulation tickingRequest(@NotNull IGridNode node, int ticksSinceLastCall) {
		if (!this.getProxy().isReady()) {return TickRateModulation.SLEEP;}
		var isConnectionValid = this.connection != null;

		var myNode = this.getGridNode();
		if (myNode == null) {
			AELog.error("what the hell, where's my node");
			return TickRateModulation.SLOWER;
		}
		IPartHost host = this.getHost();
		AEPartLocation side = this.getSide();

		var loc = host.getTile().getPos();
		var dir = side.getFacing();
		var world = host.getLocation().getWorld();
		var opposite = side.getOpposite();
		var blockSet = new HashSet<BlockPos>();

		for (int i = 0; i < 32; i++) {
			loc = loc.offset(dir);

			var te = world.getTileEntity(loc);
			if (te instanceof IPartHost ph) {
				var part = ph.getPart(opposite);
				if (part instanceof PartBeamFormer potentialFormer) {
					if (isConnectionValid && potentialFormer == otherBeamFormer && otherBeamFormer.otherBeamFormer == this) {
						// we're good.
						return TickRateModulation.SLEEP;
					}

					// We're not ok.
					disconnect();

					if (potentialFormer.getProxy().isReady()) {
						try {
							this.connection = AEApi.instance().grid().createGridConnection(myNode,
								potentialFormer.getGridNode());

							potentialFormer.connection = this.connection;
							this.otherBeamFormer = potentialFormer;
							potentialFormer.otherBeamFormer = this;

							recalcListener(blockSet);

						} catch (final FailedConnectionException | NullPointerException e) {
							// We tried. We found the beam former, but couldn't establish the connection.
							// If the former isn't ready yet, wait until it's ready, and it will try connecting
							// itself.
							AELog.error(e);
						}
					}

					// Don't go past formers.
					return TickRateModulation.SLOWER;
				}
			}

			var block = world.getBlockState(loc);
			// No can do. Something is blocking us.
			if (block.getMaterial().isOpaque()) {
				disconnect();
				return TickRateModulation.SLOWER;
			}

			blockSet.add(loc);
		}

		// We've checked the entire length. Nothing is found.
		return TickRateModulation.SLOWER;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.getCompoundTag("part").setDouble("beamLength", beamLength);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.beamLength = data.getCompoundTag("part").getDouble("beamLength");
	}
}
