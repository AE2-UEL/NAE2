package co.neeve.nae2.common.parts.p2p;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.capabilities.Capabilities;
import appeng.core.settings.TickRates;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.ItemStackHelper;
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
import co.neeve.nae2.mixin.dualityinterface.DualityAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PartP2PInterface extends PartP2PTunnel<PartP2PInterface> implements IItemHandler, IGridTickable {
	private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_redstone");
	private final MachineSource mySource;
	private final List<ItemStack> waitingToSend = new ArrayList<>();
	private int depth = 0;
	private int oldSize = 0;
	private WrapperChainedItemHandler cachedInv;
	private boolean partVisited;
	private boolean requested;
	private HashSet<PartP2PInterface> cachedOutputs;

	public PartP2PInterface(ItemStack is) {
		super(is);

		this.mySource = new MachineSource(this);
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

		NBTTagList waitingToSend = new NBTTagList();
		for (ItemStack is : this.waitingToSend) {
			NBTTagCompound itemNBT = ItemStackHelper.stackToNBT(is);
			waitingToSend.appendTag(itemNBT);
		}

		data.setTag("waitingToSend", waitingToSend);
	}

	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		NBTTagList waitingList = data.getTagList("waitingToSend", 10);
		NBTTagCompound up;
		for (int x = 0; x < waitingList.tagCount(); ++x) {
			up = waitingList.getCompoundTagAt(x);
			ItemStack is = ItemStackHelper.stackFromNBT(up);
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
		boolean wasReq = this.requested;
		if (this.requested && this.cachedInv != null) {
			this.cachedInv.cycleOrder();
		}

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
				if (target instanceof IInterfaceHost || target instanceof TileCableBus && ((TileCableBus) target).getPart(s.getOpposite()) instanceof PartInterface) {
					try {
						IInterfaceHost targetTE;
						if (target instanceof IInterfaceHost) {
							targetTE = (IInterfaceHost) target;
						} else {
							targetTE = (IInterfaceHost) ((TileCableBus) target).getPart(s.getOpposite());
						}

						var dualityAccessor = (DualityAccessor) targetTE.getInterfaceDuality();

						if (!dualityAccessor.invokeSameGrid(this.getGridNode().getGrid())) {
							IStorageMonitorableAccessor mon =
								target.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, s.getOpposite());
							if (mon != null) {
								IStorageMonitorable sm = mon.getInventory(this.mySource);
								if (sm != null && Platform.canAccess(dualityAccessor.getGridProxy(),
									this.mySource)) {
									IMEMonitor<IAEItemStack> inv =
										sm.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
									if (inv != null) {
										Iterator<ItemStack> i = this.waitingToSend.iterator();

										while (i.hasNext()) {
											ItemStack whatToSend = i.next();
											IAEItemStack result =
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
					InventoryAdaptor ad = InventoryAdaptor.getAdaptor(target, s.getOpposite());
					Iterator<ItemStack> i = this.waitingToSend.iterator();

					while (i.hasNext()) {
						ItemStack whatToSend = i.next();
						if (ad != null) {
							ItemStack result = ad.addItems(whatToSend);
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
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		super.getDrops(drops, wrenched);

		drops.addAll(this.waitingToSend);
	}

	@Nullable
	public TileEntity getFacingTileEntity() {
		var tile = this.getTile();

		return tile.getWorld().getTileEntity(tile.getPos().offset(getSide().getFacing()));
	}

	public boolean hasCapability(Capability<?> capabilityClass) {
		return capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capabilityClass);
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capabilityClass) {
		return capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this :
			super.getCapability(capabilityClass);
	}

	public int getSlots() {
		return this.getDestination().getSlots();
	}

	public @NotNull ItemStack getStackInSlot(int i) {
		return this.getDestination().getStackInSlot(i);
	}

	/**
	 * Carbon copy of PartP2PItems. Except this only gathers ME Interfaces from inputs and chains them.
	 */
	private IItemHandler getDestination() {
		this.requested = true;
		if (this.cachedInv != null) {
			return this.cachedInv;
		} else {
			List<IItemHandler> outs = new ArrayList<>();

			TunnelCollection<PartP2PInterface> itemTunnels;
			try {
				itemTunnels = this.getInputs();
			} catch (GridAccessException ignored) {
				return EmptyHandler.INSTANCE;
			}

			for (PartP2PInterface tunnel : itemTunnels) {
				IItemHandler inv = tunnel.getOutputInv();
				if (inv != null && inv != this) {
					if (Platform.getRandomInt() % 2 == 0) {
						outs.add(inv);
					} else {
						outs.add(0, inv);
					}
				}
			}

			return this.cachedInv =
				new WrapperChainedItemHandler(outs.toArray(new IItemHandler[outs.size()]));
		}
	}

	/**
	 * Gets a reference to the ME Interface directly in front of the input.
	 */
	@Nullable
	private IItemHandler getOutputInv() {
		IItemHandler ret = null;
		if (!this.partVisited) {
			this.partVisited = true;
			if (this.getProxy().isActive()) {
				EnumFacing facing = this.getSide().getFacing();
				TileEntity te = this.getTile().getWorld().getTileEntity(this.getTile().getPos().offset(facing));
				if ((te instanceof IInterfaceHost || te instanceof TileCableBus && ((TileCableBus) te).getPart(facing.getOpposite()) instanceof PartInterface) && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
					facing.getOpposite())) ret = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
					facing.getOpposite());
			}

			this.partVisited = false;
		}

		return ret;
	}

	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (this.depth == 1) {
			return stack;
		} else {
			++this.depth;
			ItemStack ret = this.getDestination().insertItem(slot, stack, simulate);
			--this.depth;
			return ret;
		}
	}

	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		return this.getDestination().extractItem(slot, amount, simulate);
	}

	public int getSlotLimit(int slot) {
		return this.getDestination().getSlotLimit(slot);
	}

	public boolean isValidDestination(PartP2PInterface tunnel) {
		if (this.cachedOutputs == null) {
			var outputs = this.getOutputs();

			if (outputs != null) {
				var hs = new HashSet<PartP2PInterface>();
				outputs.forEach(hs::add);
				this.cachedOutputs = hs;
			}
		}

		return this.cachedOutputs != null && this.cachedOutputs.contains(tunnel);
	}

	public void onTunnelNetworkChange() {
		if (!this.isOutput()) {
			this.cachedInv = null;
			this.cachedOutputs = null;
			int olderSize = this.oldSize;
			this.oldSize = this.getDestination().getSlots();
			if (olderSize != this.oldSize) {
				this.getHost().notifyNeighbors();
			}
		} else {
			try {

				for (PartP2PInterface partP2PInterface : this.getInputs()) {
					if (partP2PInterface != null) {
						partP2PInterface.getHost().notifyNeighbors();
					}
				}
			} catch (GridAccessException var3) {
				var3.printStackTrace();
			}
		}
	}

	public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
		this.cachedInv = null;

		try {
			if (this.isOutput()) {

				for (PartP2PInterface partP2PInterface : this.getInputs()) {
					if (partP2PInterface != null) {
						partP2PInterface.onTunnelNetworkChange();
					}
				}
			}
		} catch (GridAccessException var6) {
			var6.printStackTrace();
		}
	}
}
