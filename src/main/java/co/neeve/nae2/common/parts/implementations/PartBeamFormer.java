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
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.parts.PartModel;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.rendering.BeamRenderer;
import co.neeve.nae2.common.features.subfeatures.BeamFeatures;
import co.neeve.nae2.common.parts.NAEBasePartState;
import co.neeve.nae2.server.IBlockStateListener;
import co.neeve.nae2.server.WorldListener;
import com.google.common.collect.ImmutableList;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.utils.BloomEffectUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

public class PartBeamFormer extends NAEBasePartState implements IBlockStateListener, IGridTickable {
	// Resources.
	public static final ResourceLocation PRISM_LOC =
		new ResourceLocation(Tags.MODID, "part/beam_former_prism");
	private static final ResourceLocation STATUS_OFF_LOC =
		new ResourceLocation(Tags.MODID, "part/beam_former_status_off");
	private static final ResourceLocation STATUS_ON_LOC =
		new ResourceLocation(Tags.MODID, "part/beam_former_status_on");
	private static final ResourceLocation STATUS_BEAMING_LOC =
		new ResourceLocation(Tags.MODID, "part/beam_former_status_beaming");

	// Models.
	private static final ResourceLocation MODEL_BASE_LOC =
		new ResourceLocation(Tags.MODID, "part/beam_former_base");
	public static final PartModel MODEL_BEAMING = new PartModel(STATUS_BEAMING_LOC, MODEL_BASE_LOC);
	public static final PartModel MODEL_ON = new PartModel(STATUS_ON_LOC, MODEL_BASE_LOC, PRISM_LOC);
	public static final PartModel MODEL_OFF = new PartModel(STATUS_OFF_LOC, MODEL_BASE_LOC, PRISM_LOC);

	@SideOnly(Side.CLIENT)
	private static Object GREGTECH_BLOOM_HANDLER;

	static {
		if (Platform.isClientInstall()) {
			GREGTECH_BLOOM_HANDLER = null;
			if (BeamFeatures.GREGTECH_SHADERS.isEnabled()) {
				try {
					GREGTECH_BLOOM_HANDLER = new BloomEffectUtil.IBloomRenderFast() {
						float lastBrightnessX;
						float lastBrightnessY;

						@Override
						@SideOnly(Side.CLIENT)
						public int customBloomStyle() {
							return 1;
						}

						@Override
						@SideOnly(Side.CLIENT)
						public void preDraw(BufferBuilder buffer) {
							BloomEffect.strength = 0.8f;
							BloomEffect.baseBrightness = 0.0f;
							BloomEffect.highBrightnessThreshold = 1.25f;
							BloomEffect.lowBrightnessThreshold = 0.5f;
							BloomEffect.step = 1;

							this.lastBrightnessX = OpenGlHelper.lastBrightnessX;
							this.lastBrightnessY = OpenGlHelper.lastBrightnessY;

							OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
							GlStateManager.disableTexture2D();
						}

						@Override
						@SideOnly(Side.CLIENT)
						public void postDraw(BufferBuilder buffer) {
							GlStateManager.enableTexture2D();
							OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, this.lastBrightnessX,
								this.lastBrightnessY);
						}
					};
				} catch (NoClassDefFoundError ignored) {}
			}
		}
	}

	private int beamLength = 0;
	private PartBeamFormer otherBeamFormer = null;
	private IGridConnection connection = null;
	private Long2ObjectLinkedOpenHashMap<BlockPos> listenerLinkedList = null;
	private boolean hideBeam;
	@SideOnly(Side.CLIENT)
	private boolean paired;

	public PartBeamFormer(ItemStack is) {
		super(is);
		this.getProxy().setFlags(GridFlags.PREFERRED);
		this.updateEnergyConsumption();
	}

	@PartModels
	public static List<IPartModel> getModels() {
		return ImmutableList.of(MODEL_ON, MODEL_OFF, MODEL_BEAMING);
	}

	@SuppressWarnings("deprecation")
	private static boolean isTranslucent(IBlockState newState) {
		return !newState.getMaterial().isOpaque() || newState.getBlock().getLightOpacity(newState) != 255;
	}

	@SideOnly(Side.CLIENT)
	public static void drawCube(BufferBuilder bufferBuilder, double x, double y, double z, double scaleX,
	                            double scaleY, double scaleZ, float r, float g, float b) {
		// Calculate the offset for each axis
		var halfScaleX = scaleX / 2;
		var halfScaleY = scaleY / 2;
		var halfScaleZ = scaleZ / 2;

		// Bottom vertices of the cube
		var x1 = x - halfScaleX;
		var y1 = y - halfScaleY;
		var z1 = z - halfScaleZ;

		// Top vertices of the cube
		var x2 = x + halfScaleX;
		var y2 = y + halfScaleY;
		var z2 = z + halfScaleZ;

		// Down face
		var alpha = 0.9f;
		bufferBuilder.pos(x1, y1, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y1, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y1, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x1, y1, z2).color(r, g, b, alpha).endVertex();

		// Up face
		bufferBuilder.pos(x1, y2, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x1, y2, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y2, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y2, z1).color(r, g, b, alpha).endVertex();

		// North face
		bufferBuilder.pos(x1, y1, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x1, y2, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y2, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y1, z1).color(r, g, b, alpha).endVertex();

		// South face
		bufferBuilder.pos(x1, y1, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y1, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y2, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x1, y2, z2).color(r, g, b, alpha).endVertex();

		// West face
		bufferBuilder.pos(x1, y1, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x1, y1, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x1, y2, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x1, y2, z1).color(r, g, b, alpha).endVertex();

		// East face
		bufferBuilder.pos(x2, y1, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y2, z1).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y2, z2).color(r, g, b, alpha).endVertex();
		bufferBuilder.pos(x2, y1, z2).color(r, g, b, alpha).endVertex();
	}

	public @NotNull IPartModel getStaticModels() {
		return !(this.isActive() && this.isPowered()) ? MODEL_OFF : (this.paired ? MODEL_BEAMING : MODEL_ON);
	}

	@Override
	public void getBoxes(final IPartCollisionHelper bch) {
		bch.addBox(10, 10, 12, 6, 6, 11);
		bch.addBox(10, 10, 13, 6, 6, 12);
		bch.addBox(10, 6, 14, 6, 5, 13);
		bch.addBox(11, 9, 17, 10, 7, 14);
		bch.addBox(9, 11, 17, 7, 10, 14);
		bch.addBox(6, 9, 17, 5, 7, 14);
		bch.addBox(9, 6, 17, 7, 5, 14);
		bch.addBox(10, 11, 14, 6, 10, 13);
		bch.addBox(6, 10, 14, 5, 6, 13);
		bch.addBox(11, 9, 13, 10, 7, 12);
		bch.addBox(6, 9, 13, 5, 7, 12);
		bch.addBox(9, 6, 13, 7, 5, 12);
		bch.addBox(9, 11, 13, 7, 10, 12);
		bch.addBox(11, 10, 14, 10, 6, 13);
	}

	@Override
	public float getCableConnectionLength(AECableType cable) {
		return 5f;
	}

	@Override
	public void removeFromWorld() {
		this.unregisterListener();
		this.disconnect(null);
		this.getProxy().invalidate();
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		try {
			this.getProxy().getTick().alertDevice(this.getGridNode());
		} catch (GridAccessException ignored) {
		}
	}

	@Override
	public int getLightLevel() {
		return !this.hideBeam
			&& ((Platform.isClient() && this.paired) || this.beamLength != 0 || this.otherBeamFormer != null)
			&& (this.isActive() && this.isPowered()) ? 15 : 0;
	}

	@Override
	public void setPartHostInfo(AEPartLocation side, IPartHost host, TileEntity tile) {
		super.setPartHostInfo(side, host, tile);
	}

	private void updateEnergyConsumption() {
		// Up to 96 AE/t per pair. Otherwise, 2 AE idle usage (pairing mode yo).
		this.getProxy().setIdlePowerUsage(Math.pow(Math.max(2d, this.beamLength), 1.05));
	}

	private void unregisterListener() {
		WorldListener.instance.unregisterBlockStateListener(this);
	}

	private void connect(PartBeamFormer potentialFormer, Iterable<BlockPos> locs) throws FailedConnectionException {
		// Form the connection.
		var myNode = this.getGridNode();
		this.connection = AEApi.instance().grid().createGridConnection(myNode,
			potentialFormer.getGridNode());

		potentialFormer.connection = this.connection;
		this.otherBeamFormer = potentialFormer;
		potentialFormer.otherBeamFormer = this;

		// Copy over hiding.
		if (potentialFormer.hideBeam || this.hideBeam) {
			potentialFormer.hideBeam = true;
			this.hideBeam = true;
		}

		// Re-register and rehash block positions for world listening.
		this.unregisterListener();
		this.otherBeamFormer.unregisterListener();
		this.listenerLinkedList = new Long2ObjectLinkedOpenHashMap<>();
		for (var loc : locs)
			this.listenerLinkedList.put(loc.toLong(), loc);

		WorldListener.instance.registerBlockStateListener(this, locs);

		this.beamLength = this.listenerLinkedList.size();
		this.otherBeamFormer.beamLength = 0;

		// eh
		this.updateEnergyConsumption();
		this.otherBeamFormer.updateEnergyConsumption();
		try {
			this.otherBeamFormer.getProxy().getTick().sleepDevice(this.otherBeamFormer.getGridNode());
		} catch (GridAccessException ignored) {}

		this.getHost().markForUpdate();
		this.getHost().markForSave();
		this.otherBeamFormer.getHost().markForUpdate();
		this.otherBeamFormer.getHost().markForSave();
	}

	public boolean disconnect(@Nullable BlockPos breakPos) {
		if (this.connection == null) {
			this.updateEnergyConsumption();
			return false;
		}

		var newBeamA = 0;
		var newBeamB = 0;

		if (breakPos != null) {
			var iterator = this.listenerLinkedList.long2ObjectEntrySet().fastIterator();
			var hash = breakPos.toLong();
			while (iterator.hasNext()) {
				if (iterator.next().getLongKey() == hash) break;
				newBeamA++;
			}

			while (iterator.hasNext()) {
				iterator.next();
				newBeamB++;
			}
		}

		this.beamLength = newBeamA;
		if (this.connection != null) {
			this.connection.destroy();
			this.connection = null;
		}
		this.updateEnergyConsumption();
		this.getHost().markForUpdate();
		this.getHost().markForSave();

		if (this.otherBeamFormer != null && this.otherBeamFormer.otherBeamFormer == this) {
			this.otherBeamFormer.beamLength = newBeamB;
			this.otherBeamFormer.connection = null;
			this.otherBeamFormer.otherBeamFormer = null;
			this.otherBeamFormer.updateEnergyConsumption();
			this.otherBeamFormer.getHost().markForUpdate();
			this.otherBeamFormer.getHost().markForSave();
			this.otherBeamFormer = null;
		}

		return true;
	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		try {
			var isValid = isTranslucent(newState);

			if (isValid) {
				var te = this.getTile().getWorld().getTileEntity(pos);
				if (te instanceof IPartHost partHost) {
					if (partHost.getPart(this.getSide()) instanceof PartBeamFormer ||
						partHost.getPart(this.getSide().getOpposite()) instanceof PartBeamFormer) {
						isValid = false;
					}
				}
			}

			if (this.connection != null && !isValid) {
				this.disconnect(pos);
				this.getProxy().getTick().alertDevice(this.getGridNode());
			} else if (isValid && this.connection == null) {
				this.getProxy().getTick().alertDevice(this.getGridNode());
			}
		} catch (GridAccessException ignored) {}
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (hand == EnumHand.MAIN_HAND && Platform.isWrench(player, player.getHeldItemMainhand(), new BlockPos(pos))) {
			if (Platform.isServer()) {
				this.hideBeam = !this.hideBeam;

				player.sendMessage(new TextComponentTranslation(this.hideBeam ? "nae2.part.beam_former.hide" :
					"nae2.part.beam_former.show"));
				this.getHost().markForUpdate();
				this.getHost().markForSave();

				if (this.otherBeamFormer != null) {
					this.otherBeamFormer.hideBeam = this.hideBeam;
					this.otherBeamFormer.getHost().markForUpdate();
					this.otherBeamFormer.getHost().markForSave();
				}
			}

			player.swingArm(hand);
			return !player.world.isRemote;
		}

		return super.onPartActivate(player, hand, pos);
	}

	@NotNull
	@Override
	public TickingRequest getTickingRequest(@NotNull IGridNode node) {
		return new TickingRequest(20, 300, false, true);
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

	@NotNull
	@Override
	public TickRateModulation tickingRequest(@NotNull IGridNode node, int ticksSinceLastCall) {
		if (!this.getProxy().isReady()) return TickRateModulation.SAME;

		var isConnectionValid = this.connection != null;

		var myNode = this.getGridNode();
		if (myNode == null) {
			AELog.error("what the hell, where's my node");
			return TickRateModulation.SLOWER;
		}
		var host = this.getHost();
		var side = this.getSide();

		var loc = host.getTile().getPos();
		var dir = side.getFacing();
		var world = host.getLocation().getWorld();
		var opposite = side.getOpposite();
		var blockSet = new LinkedHashSet<BlockPos>();

		for (var i = 0; i < 32; i++) {
			loc = loc.offset(dir);

			var te = world.getTileEntity(loc);
			if (te instanceof IPartHost ph) {
				var part = ph.getPart(opposite);
				if (part instanceof PartBeamFormer potentialFormer) {
					if (isConnectionValid && potentialFormer == this.otherBeamFormer && this.otherBeamFormer.otherBeamFormer == this) {
						// we're good.
						return TickRateModulation.SLEEP;
					}

					// We're not ok.
					var disconnected = this.disconnect(loc);

					if (potentialFormer.getProxy().isReady() && potentialFormer.otherBeamFormer == null) {
						try {
							this.connect(potentialFormer, blockSet);
							return TickRateModulation.SLEEP;

						} catch (final FailedConnectionException | NullPointerException e) {
							// We tried. We found the beam former, but couldn't establish the connection.
							// If the former isn't ready yet, wait until it's ready, and it will try connecting
							// itself.
							AELog.error(e);
						}
					}

					// Don't go past formers.
					return disconnected ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
				}

				if (ph.getPart(side) instanceof PartBeamFormer) {
					return this.disconnect(loc) ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
				}
			}

			var block = world.getBlockState(loc);
			// No can do. Something is blocking us.
			if (!isTranslucent(block)) {
				return this.disconnect(loc) ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
			}

			blockSet.add(loc);
		}

		// We've checked the entire length. Nothing is found.
		return TickRateModulation.SLOWER;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(double x, double y, double z, float partialTicks, int destroyStage) {
		if (!this.hideBeam && this.beamLength != 0 && this.isActive() && this.isPowered()) {
			// Get the direction vector
			var facing = this.getSide().getFacing();
			final var dx = facing.getXOffset();
			final var dy = facing.getYOffset();
			final var dz = facing.getZOffset();

			if (GREGTECH_BLOOM_HANDLER != null) {
				BloomEffectUtil.requestCustomBloom((BloomEffectUtil.IBloomRenderFast) GREGTECH_BLOOM_HANDLER,
					bufferBuilder -> this.drawBeamGT(x, y, z, dx, dy, dz, bufferBuilder));
				return;
			}

			var color = this.getHost().getColor();
			var scale = 255f;
			var rgb = new float[]{ ((color.mediumVariant >> 16) & 0xff) / scale,
				((color.mediumVariant >> 8) & 0xff) / scale,
				(color.mediumVariant & 0xff) / scale };

			// Calculate the pitch, yaw, and roll based on the direction vector
			var pitch = (float) Math.atan2(Math.sqrt(dx * dx + dz * dz), dy) * (180F / (float) Math.PI);
			var yaw = (float) (180 - Math.atan2(dz, dx) * (180F / (float) Math.PI) - 90.0F);

			GlStateManager.pushMatrix();

			// Translate and rotate
			GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
			GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(-0.5, 0.35, -0.5);

			BeamRenderer.renderBeamSegment(0, 0, 0, partialTicks, 1,
				(double) this.getHost().getTile().getWorld().getTotalWorldTime(), 0,
				this.beamLength + 0.3d
				, rgb, 0.075 * 1.6, 0.075 * 2);

			GlStateManager.popMatrix();
		}
	}

	@SideOnly(Side.CLIENT)
	private void drawBeamGT(double x, double y, double z, int dx, int dy, int dz, BufferBuilder bufferBuilder) {
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		var bl = (this.beamLength + 1) / 2d;
		var minScale = 0.15d;
		var color = this.getHost().getColor();
		var scale = 255f;
		var rgb = new float[]{ ((color.mediumVariant >> 16) & 0xff) / scale,
			((color.mediumVariant >> 8) & 0xff) / scale,
			(color.mediumVariant & 0xff) / scale };

		drawCube(bufferBuilder,
			x + 0.5d + dx * bl,
			y + 0.5d + dy * bl,
			z + 0.5d + dz * bl,
			Math.max(minScale, Math.abs(dx * this.beamLength + dx * 0.25d)),
			Math.max(minScale, Math.abs(dy * this.beamLength + dy * 0.25d)),
			Math.max(minScale, Math.abs(dz * this.beamLength + dz * 0.25d)),
			rgb[0],
			rgb[1],
			rgb[2]
		);

		GlStateManager.color(1f, 1f, 1f, 1f);
		Tessellator.getInstance().draw();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requireDynamicRender() {
		return true;
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		var shouldRedraw = super.readFromStream(data);

		this.beamLength = data.readInt();
		var wasPaired = this.paired;
		this.paired = data.readBoolean();
		// Kick rendering.
		if (this.paired != wasPaired) {
			var pos = this.getTile().getPos();
			var x = pos.getX();
			var y = pos.getY();
			var z = pos.getZ();
			Minecraft.getMinecraft().renderGlobal
				.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
		}
		this.hideBeam = data.readBoolean();

		return shouldRedraw;
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		data.writeInt(this.beamLength);
		data.writeBoolean(this.otherBeamFormer != null);
		data.writeBoolean(this.hideBeam);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		var part = data.getCompoundTag("part");
		if (this.beamLength > 0) part.setInteger("beamLength", this.beamLength);
		if (this.hideBeam) part.setBoolean("hideBeam", true);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		var part = data.getCompoundTag("part");
		// Back compat.
		if (part.getTag("beamLength") instanceof NBTTagDouble dbl) {
			this.beamLength = (int) dbl.getDouble();
		} else {
			this.beamLength = part.getInteger("beamLength");
		}
		this.hideBeam = part.getBoolean("hideBeam");
	}
}
