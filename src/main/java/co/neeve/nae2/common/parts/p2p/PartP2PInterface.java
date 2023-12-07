package co.neeve.nae2.common.parts.p2p;

import akka.japi.Pair;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.capabilities.Capabilities;
import appeng.core.settings.TickRates;
import appeng.fluids.helper.IFluidInterfaceHost;
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
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.common.helpers.WrapperChainedFluidHandler;
import co.neeve.nae2.mixin.ifacep2p.shared.DualityAccessor;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PartP2PInterface extends PartP2PTunnel<PartP2PInterface> implements IItemHandler, IGridTickable,
	IFluidHandler {
	public static final ObjectOpenHashSet<Capability<?>> SUPPORTED_CAPABILITIES = new ObjectOpenHashSet<Capability<?>>(
		new Capability[]{
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
			CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
		});
	private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_interface");
	private final MachineSource mySource;
	private final List<ItemStack> waitingToSend = new ArrayList<>();
	private final CapabilityCache capabilityCache;
	private int depth = 0;

	private boolean requested;
	private ObjectOpenHashSet<PartP2PInterface> cachedOutputs;
	private IItemHandler cachedInv;
	private IFluidHandler cachedTank;
	private ObjectOpenHashSet<PartP2PInterface> cachedInputs;

	public PartP2PInterface(ItemStack is) {
		super(is);

		this.mySource = new MachineSource(this);
		this.capabilityCache = new CapabilityCache();
	}

	@PartModels
	public static List<IPartModel> getModels() {
		return MODELS.getModels();
	}

	private static <T> ObjectOpenHashSet<T> gatherCapabilities(Collection<PartP2PInterface> tunnels,
	                                                           Capability<T> capabilityType) {
		var caps = new ObjectOpenHashSet<T>();
		for (var tunnel : tunnels) {
			var optionalTileEntity = tunnel.getFacingTileEntity();
			if (!optionalTileEntity.isPresent()) continue;

			var facing = tunnel.getSide().getOpposite().getFacing();
			var te = optionalTileEntity.get();
			if (!isInterface(te, facing)) continue;

			var capability = te.getCapability(capabilityType, facing);
			if (capability != null) {
				caps.add(capability);
			}
		}
		return caps;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] toArray(Set<T> set, Class<T> clazz) {
		var array = (T[]) java.lang.reflect.Array.newInstance(clazz, set.size());
		var i = 0;
		for (var item : set) {
			array[i++] = item;
		}
		return array;
	}

	public static boolean isInterface(Object te, EnumFacing facing) {
		if (te instanceof IInterfaceHost) return true;
		if (te instanceof IFluidInterfaceHost) return true;

		if (te instanceof IPartHost iPartHost) {
			var part = iPartHost.getPart(facing);
			return isInterface(part, facing);
		}

		return false;
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

		if (this.hasWorkToDo())
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
			var s = this.getSide().getFacing();
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

	private boolean hasWorkToDo() {
		return this.hasItemsToSend();
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

	public Optional<TileEntity> getFacingTileEntity() {
		var tile = this.getTile();

		return Optional.ofNullable(tile.getWorld().getTileEntity(tile.getPos().offset(this.getSide().getFacing())));
	}

	public boolean hasCapability(Capability<?> capabilityClass) {
		return SUPPORTED_CAPABILITIES.contains(capabilityClass) || super.hasCapability(capabilityClass);
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capabilityClass) {
		return SUPPORTED_CAPABILITIES.contains(capabilityClass) ? (T) this : super.getCapability(capabilityClass);
	}

	private IItemHandler getItemHandler() {
		if (this.cachedInv == null) {
			this.cachedInv = EmptyHandler.INSTANCE;

			for (var input : this.getCachedInputs()) {
				var pair = input.capabilityCache.get(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				pair.ifPresent(objectOpenHashSetObjectPair ->
					this.cachedInv = (IItemHandler) objectOpenHashSetObjectPair.second());
			}
		}
		return this.cachedInv;
	}

	private IFluidHandler getFluidHandler() {
		if (this.cachedTank == null) {
			this.cachedTank = EmptyFluidHandler.INSTANCE;

			for (var input : this.getCachedInputs()) {
				var pair = input.capabilityCache.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
				pair.ifPresent(objectOpenHashSetObjectPair ->
					this.cachedTank = (IFluidHandler) objectOpenHashSetObjectPair.second());
			}

		}
		return this.cachedTank;
	}

	public boolean isValidDestination(PartP2PInterface tunnel) {
		return this.getCachedOutputs().contains(tunnel);
	}

	private ObjectOpenHashSet<PartP2PInterface> getCachedOutputs() {
		if (this.cachedOutputs == null) {
			this.cachedOutputs = new ObjectOpenHashSet<>();

			var outputs = this.getOutputs();
			if (outputs != null) {
				outputs.forEach(this.cachedOutputs::add);
			}
		}
		return this.cachedOutputs;
	}

	private ObjectOpenHashSet<PartP2PInterface> getCachedInputs() {
		if (this.cachedInputs == null) {
			this.cachedInputs = new ObjectOpenHashSet<>();

			var inputs = this.getInputs();
			if (inputs != null) {
				inputs.forEach(this.cachedInputs::add);
			}
		}
		return this.cachedInputs;
	}

	public void onTunnelNetworkChange() {
		this.cachedInputs = null;
		this.cachedOutputs = null;

		if (this.isOutput()) {
			this.cachedInv = null;
			this.cachedTank = null;
			this.getHost().notifyNeighbors();
		}
	}

	public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
		if (!this.isOutput()) {
			var changed = false;

			for (var capability : SUPPORTED_CAPABILITIES) {
				changed |= this.processCapabiltiies(this.getCachedInputs(), capability);
			}

			if (changed) {
				for (var tunnel : this.getCachedOutputs()) {
					tunnel.onTunnelNetworkChange();
				}
			}
		}
	}

	private <T> boolean processCapabiltiies(Collection<PartP2PInterface> inputs, Capability<T> capability) {
		final var caps = gatherCapabilities(inputs, capability);

		if (!this.capabilityCache.isSameAsCached(capability, caps)) {
			final var handler = this.createHandler(capability, caps);
			this.capabilityCache.store(capability, caps, handler);
			for (var input : inputs) {
				input.capabilityCache.store(capability, caps, handler);
			}

			return true;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> getCapabilityHandlerClass(Capability<T> capability) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (Class<T>) IItemHandler.class;
		} else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (Class<T>) IFluidHandler.class;
		} else {
			throw new RuntimeException("Unexpected capability: " + capability);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T createHandler(Capability<T> capability, ObjectOpenHashSet<T> caps) {
		var clazz = this.getCapabilityHandlerClass(capability);
		var array = toArray(caps, clazz);

		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) new WrapperChainedItemHandler((IItemHandler[]) array);
		} else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T) new WrapperChainedFluidHandler((IFluidHandler[]) array);
		}
		return null;
	}

	// Capability implementations

	public int getSlots() {
		return this.getItemHandler().getSlots();
	}

	public @NotNull ItemStack getStackInSlot(int i) {
		return this.getItemHandler().getStackInSlot(i);
	}

	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (!this.isOutput()) return stack;

		if (this.depth == 1) {
			return stack;
		} else {
			++this.depth;
			var ret = this.getItemHandler().insertItem(slot, stack, simulate);
			--this.depth;
			return ret;
		}
	}

	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (!this.isOutput()) return ItemStack.EMPTY;

		return this.getItemHandler().extractItem(slot, amount, simulate);
	}

	public int getSlotLimit(int slot) {
		return this.getItemHandler().getSlotLimit(slot);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return this.getFluidHandler().getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		return this.getFluidHandler().fill(resource, doFill);
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack fluid, boolean doDrain) {
		return this.getFluidHandler().drain(fluid, doDrain);
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return this.getFluidHandler().drain(maxDrain, doDrain);
	}

	private static class CapabilityCache {
		private final Object2ObjectOpenHashMap<Capability<?>, ObjectOpenHashSet<?>> cachedCapabilities =
			new Object2ObjectOpenHashMap<>();
		private final Object2ObjectOpenHashMap<Capability<?>, Object> cachedHandlers =
			new Object2ObjectOpenHashMap<>();

		public <T> void store(Capability<T> capability, ObjectOpenHashSet<T> capabilities, T handler) {
			this.cachedCapabilities.put(capability, capabilities);
			this.cachedHandlers.put(capability, handler);
		}

		@SuppressWarnings("unchecked")
		public <T> Optional<Pair<ObjectOpenHashSet<T>, Object>> get(Capability<T> capability) {
			var set = this.cachedCapabilities.getOrDefault(capability, null);
			if (set != null) {
				return Optional.of(Pair.apply((ObjectOpenHashSet<T>) set, this.cachedHandlers.get(capability)));
			}

			return Optional.empty();
		}

		public <T> boolean isSameAsCached(Capability<T> capability, @Nullable ObjectOpenHashSet<T> capabilities) {
			var cached = this.get(capability);
			var present = cached.isPresent();
			if (capabilities == null && present) return false;
			if (capabilities != null && !present) return false;

			return capabilities != null && capabilities.equals(cached.get().first());
		}
	}
}
