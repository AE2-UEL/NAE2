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
import appeng.util.item.AEItemStack;
import co.neeve.nae2.mixin.dualityinterface.DualityAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PartP2PInterface extends PartP2PTunnel<PartP2PInterface> implements IGridTickable {
	private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_redstone");
	private final MachineSource mySource;
	private final List<ItemStack> waitingToSend = new ArrayList<>();

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
			} catch (GridAccessException ignored) {
			}
		}
	}

	public @NotNull TickingRequest getTickingRequest(@NotNull IGridNode node) {
		return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), !this.hasWorkToDo(),
			true);
	}

	public @NotNull TickRateModulation tickingRequest(@NotNull IGridNode node, int ticksSinceLastCall) {
		if (!this.getProxy().isActive()) {
			return TickRateModulation.SLEEP;
		} else {
			var worked = false;
			if (this.hasItemsToSend()) {
				worked = this.pushItemsOut();
			}

			return this.hasWorkToDo() ? (worked ? TickRateModulation.URGENT : TickRateModulation.SLOWER) :
				TickRateModulation.SLEEP;
		}
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
}
