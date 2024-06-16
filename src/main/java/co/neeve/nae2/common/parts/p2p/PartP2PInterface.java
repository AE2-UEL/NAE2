package co.neeve.nae2.common.parts.p2p;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.util.AEPartLocation;
import appeng.capabilities.Capabilities;
import appeng.core.settings.TickRates;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.ItemStackHelper;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.me.helpers.MachineSource;
import appeng.parts.misc.PartInterface;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.tile.networking.TileCableBus;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.common.helpers.inv.FluidHandlerDelegate;
import co.neeve.nae2.common.helpers.inv.ItemHandlerDelegate;
import co.neeve.nae2.common.parts.p2p.iface.InterfaceTunnelGridCache;
import co.neeve.nae2.mixin.ifacep2p.shared.DualityAccessor;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartP2PInterface extends PartP2PTunnel<PartP2PInterface> implements IGridTickable {
	private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_interface");
	private final MachineSource mySource;
	private final List<ItemStack> waitingToSend = new ArrayList<>();
	private final ItemHandlerDelegate myItemHandlerDelegate = new ItemHandlerDelegate();
	private final FluidHandlerDelegate myFluidHandlerDelegate = new FluidHandlerDelegate();
	private EnumFacing myFacing;
	private boolean requested;
	private ObjectOpenHashSet<PartP2PInterface> cachedOutputs;
	private TileEntity facingTileEntity;
	private boolean isViewing;
	private Collection<PartP2PInterface> cachedOutputsRecursive;

	public PartP2PInterface(ItemStack is) {
		super(is);

		this.mySource = new MachineSource(this);
	}

	@PartModels
	public static List<IPartModel> getModels() {
		return MODELS.getModels();
	}

	@Override
	public void setPartHostInfo(AEPartLocation side, IPartHost host, TileEntity tile) {
		super.setPartHostInfo(side, host, tile);

		if (Platform.isClient()) return;

		this.myFacing = side.getFacing();
	}

	public EnumFacing getFacing() {
		return this.myFacing;
	}

	public @NotNull IPartModel getStaticModels() {
		return MODELS.getModel(this.isPowered(), this.isActive());
	}

	public boolean hasItemsToSend() {
		return !this.waitingToSend.isEmpty();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		var waitingToSend = new NBTTagList();
		for (var is : this.waitingToSend) {
			var itemNBT = ItemStackHelper.stackToNBT(is);
			waitingToSend.appendTag(itemNBT);
		}

		data.setTag("waitingToSend", waitingToSend);
	}

	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		var waitingList = data.getTagList("waitingToSend", 10);
		NBTTagCompound up;
		for (var x = 0; x < waitingList.tagCount(); ++x) {
			up = waitingList.getCompoundTagAt(x);
			var is = ItemStackHelper.stackFromNBT(up);
			this.addToSendList(is);
		}
	}

	/**
	 * Adds item to be pushed out. Mainly for communicating with DualityInterface.
	 */
	public void addToSendList(ItemStack is) {
		if (!is.isEmpty()) {
			this.waitingToSend.add(is);

			try {
				this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
			} catch (GridAccessException ignored) {}
		}
	}

	public @NotNull TickingRequest getTickingRequest(@NotNull IGridNode node) {
		// bleh.
		var min = Math.min(TickRates.ItemTunnel.getMin(), TickRates.Interface.getMin());
		var max = Math.max(TickRates.ItemTunnel.getMax(), TickRates.Interface.getMax());

		return new TickingRequest(min, max, false, false);
	}

	public @NotNull TickRateModulation tickingRequest(@NotNull IGridNode node, int ticksSinceLastCall) {
		// ME Interface push/pull, boring stuff. Carbon copy of item P2P.
		var wasReq = this.requested;

		this.requested = false;
		var reqResult = wasReq ? TickRateModulation.FASTER : TickRateModulation.SLOWER;

		// ME Interface pattern pushing, not boring stuff.
		var pushWorked = false;
		if (this.hasItemsToSend()) {
			pushWorked = this.pushItemsOut();
		}

		if (this.hasItemsToSend())
			return pushWorked ? TickRateModulation.URGENT : reqResult;
		return reqResult;
	}

	/**
	 * This is mostly a carbon copy of the DualityInterface method.
	 *
	 * @return Were any items pushed out?
	 */
	public boolean pushItemsOut() {
		var worked = false;

		if (!this.waitingToSend.isEmpty()) {
			var s = this.myFacing;
			var tile = this.getTile();
			var w = tile.getWorld();

			var target = w.getTileEntity(tile.getPos().offset(s));
			if (target != null) {
				// this is so bad
				if (target instanceof IInterfaceHost || target instanceof TileCableBus && ((TileCableBus) target).getPart(
					s.getOpposite()) instanceof PartInterface) {
					try {
						IInterfaceHost targetTE;
						if (target instanceof IInterfaceHost) {
							targetTE = (IInterfaceHost) target;
						} else {
							targetTE = (IInterfaceHost) ((TileCableBus) target).getPart(s.getOpposite());
						}

						var dualityAccessor = (DualityAccessor) targetTE.getInterfaceDuality();

						if (!dualityAccessor.invokeSameGrid(this.getGridNode().getGrid())) {
							var mon =
								target.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, s.getOpposite());
							if (mon != null) {
								var sm = mon.getInventory(this.mySource);
								if (sm != null && Platform.canAccess(dualityAccessor.getGridProxy(),
									this.mySource)) {
									var inv =
										sm.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
									if (inv != null) {
										var i = this.waitingToSend.iterator();

										while (i.hasNext()) {
											var whatToSend = i.next();
											var result =
												inv.injectItems(AEItemStack.fromItemStack(whatToSend),
													Actionable.MODULATE, this.mySource);
											if (result != null) {
												if (whatToSend.getCount() != result.getStackSize()) {
													worked = true;
												}

												whatToSend.setCount((int) result.getStackSize());
												whatToSend.setTagCompound(result.getDefinition().getTagCompound());
											} else {
												worked = true;
												i.remove();
											}
										}
									}
								}
							}

						}
					} catch (GridAccessException var12) {
						throw new RuntimeException(var12);
					}
				} else {
					InventoryAdaptor ad;
					if (Platform.isModLoaded("ae2fc")) {
						ad = FluidConvertingInventoryAdaptor.wrap(target, s.getOpposite());
					} else {
						ad = InventoryAdaptor.getAdaptor(target, s.getOpposite());
					}

					var i = this.waitingToSend.iterator();

					while (i.hasNext()) {
						var whatToSend = i.next();
						if (ad != null) {
							var result = ad.addItems(whatToSend);
							if (!result.isEmpty()) {
								if (whatToSend.getCount() != result.getCount()) {
									worked = true;
								}

								whatToSend.setCount(result.getCount());
								whatToSend.setTagCompound(result.getTagCompound());
							} else {
								worked = true;
								i.remove();
							}
						}
					}
				}
			}
		}

		return worked;
	}

	@Override
	@Nullable
	public TunnelCollection<PartP2PInterface> getOutputs() {
		try {
			return super.getOutputs();
		} catch (GridAccessException ignored) {
			return null;
		}
	}

	@Override
	@Nullable
	public TunnelCollection<PartP2PInterface> getInputs() {
		try {
			return super.getInputs();
		} catch (GridAccessException ignored) {
			return null;
		}
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		super.getDrops(drops, wrenched);

		drops.addAll(this.waitingToSend);
	}

	@Nullable
	public TileEntity getFacingTileEntity() {
		if (this.facingTileEntity == null) {
			try {
				var tile = this.getTile();

				this.facingTileEntity = tile.getWorld().getTileEntity(tile.getPos().offset(this.myFacing));
			} catch (NullPointerException ignored) {return null;}
		}

		return this.facingTileEntity;
	}

	public boolean isValidDestination(PartP2PInterface tunnel) {
		return this.getCachedOutputsRecursive().contains(tunnel);
	}

	public ObjectOpenHashSet<PartP2PInterface> getCachedOutputs() {
		if (this.cachedOutputs == null) {
			var outputs = this.getOutputs();
			if (outputs != null) {
				this.cachedOutputs = new ObjectOpenHashSet<>(outputs.size());
				outputs.forEach(this.cachedOutputs::add);
			} else {
				this.cachedOutputs = new ObjectOpenHashSet<>();
			}
		}
		return this.cachedOutputs;
	}

	public Collection<PartP2PInterface> getCachedOutputsRecursive() {
		if (this.cachedOutputsRecursive == null) {
			this.cachedOutputsRecursive = new ObjectOpenHashSet<>();
			this.getOutputsRecursive(this.cachedOutputsRecursive);
		}

		return this.cachedOutputsRecursive;
	}

	public void getOutputsRecursive(Collection<PartP2PInterface> collection) {
		if (this.isViewing) return;
		this.isViewing = true;

		try {
			if (!this.isOutput()) {
				var outputs = this.getCachedOutputs();
				for (var output : outputs) {
					var opposite = output.getFacing().getOpposite();
					var te = output.getFacingTileEntity();
					if (te != null) {
						if (te instanceof IPartHost partHost && partHost.getPart(opposite) instanceof PartP2PInterface ifacep2p && !ifacep2p.isOutput()) {
							ifacep2p.getOutputsRecursive(collection);
						} else {
							collection.add(output);
						}
					}
				}
			}
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
		var node = this.getGridNode();
		if (node == null) return;

		var grid = node.getGrid();
		if (grid == null) return;

		var cache = grid.getCache(InterfaceTunnelGridCache.class);
		if (cache == null) return;

		((InterfaceTunnelGridCache) cache).onNeighborChanged(this);
		this.cachedOutputsRecursive = null;
	}

	public void onTunnelNetworkChange() {
		if (Platform.isClient()) return;

		this.cachedOutputs = null;
		this.facingTileEntity = null;
		this.cachedOutputsRecursive = null;

		var cache = (InterfaceTunnelGridCache) this.getGridNode().getGrid().getCache(InterfaceTunnelGridCache.class);
		var capabilityContainer = cache.getCapabilityCacheForFreq(this.getFrequency());
		this.myItemHandlerDelegate.setDelegate(capabilityContainer);
		this.myFluidHandlerDelegate.setDelegate(capabilityContainer);

		this.getHost().notifyNeighbors();
	}

	@Override
	public boolean hasCapability(Capability<?> capabilityClass) {
		return capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
			|| capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capabilityClass) {
		if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) this.myItemHandlerDelegate;
		}

		if (capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T) this.myFluidHandlerDelegate;
		}

		return super.getCapability(capabilityClass);
	}
}
