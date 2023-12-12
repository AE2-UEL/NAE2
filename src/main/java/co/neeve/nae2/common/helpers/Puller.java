package co.neeve.nae2.common.helpers;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.inventory.AppEngInternalOversizedInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemStackHashStrategy;
import co.neeve.nae2.common.parts.implementations.IPullerHost;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class Puller implements IGridTickable, IActionHost, IAEAppEngInventory, IConfigManagerHost, IUpgradeableHost {
	private final IPullerHost host;
	private final AppEngInternalOversizedInventory inv = new AppEngInternalOversizedInventory(this, 9, 512);
	private final IActionSource machineSource = new MachineSource(this);
	private final ConfigManager cm = new ConfigManager(this);
	private EnumSet<EnumFacing> targets;

	public Puller(IPullerHost host) {
		this.host = host;
		this.cm.registerSetting(Settings.PLACE_BLOCK, YesNo.NO);
	}

	@NotNull
	@Override
	public TickingRequest getTickingRequest(@NotNull IGridNode iGridNode) {
		return new TickingRequest(1, 50, false, false);
	}

	private IItemList<IAEItemStack> getStorageList(IStorageGrid storageGrid) {
		return storageGrid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList();
	}

	@NotNull
	@Override
	public TickRateModulation tickingRequest(@NotNull IGridNode iGridNode, int ticks) {
		var refilled = this.refill();
		var pushed = this.autoPush();
		return refilled || pushed ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	private boolean refill() {
		final IStorageGrid storageGrid;
		final IEnergySource src;
		try {
			storageGrid = this.host.getProxy().getStorage();
			src = this.host.getProxy().getEnergy();
		} catch (GridAccessException e) {
			return false;
		}

		var didAnything = false;

		var inventory =
			storageGrid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
		var storageList = this.getStorageList(storageGrid);

		var lookupCache =
			new Object2ObjectOpenCustomHashMap<ItemStack, IAEItemStack>(ItemStackHashStrategy.comparingAllButCount());

		var emptySlots = new IntArrayList();
		for (var i = 0; i < this.inv.getSlots(); i++) {
			var stack = this.inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				var slotLimit = Math.min(this.inv.getSlotLimit(i), stack.getMaxStackSize() * 8);
				if (stack.getCount() >= slotLimit) continue;

				final IAEItemStack sourceStack;
				if (lookupCache.containsKey(stack)) {
					sourceStack = lookupCache.get(stack);
				} else {
					var aeis = AEItemStack.fromItemStack(stack);
					if (aeis != null) {
						sourceStack = storageList.findPrecise(aeis);
						if (sourceStack != null) {
							lookupCache.put(stack, sourceStack);
						}
					} else {
						sourceStack = null;
					}
				}

				if (sourceStack == null) {
					continue;
				}

				var toAcquire = sourceStack.copy().setStackSize(slotLimit - stack.getCount());
				var acquired = Platform.poweredExtraction(src, inventory, toAcquire, this.machineSource);
				if (acquired != null) {
					this.inv.insertItem(i, acquired.createItemStack(), false);
					didAnything = true;
				}
			} else {
				emptySlots.add(i);
			}
		}

		IAEItemStack sourceStack = null;
		var iterator = this.getIterator(storageGrid);
		for (var i : emptySlots) {
			if (sourceStack != null && sourceStack.getStackSize() <= 0) {
				sourceStack = null;
			}

			if (sourceStack == null) {
				while (iterator.hasNext()) {
					var next = iterator.next();
					if (next != null && next.getStackSize() > 0) {
						sourceStack = next;
						break;
					}
				}
			}

			if (sourceStack == null) {
				break;
			}

			var toAcquire =
				sourceStack.copy()
					.setStackSize(Math.min(this.inv.getSlotLimit(i), sourceStack
						.asItemStackRepresentation().getMaxStackSize() * 8));

			var acquired = Platform.poweredExtraction(src, inventory, toAcquire, this.machineSource);
			if (acquired != null) {
				this.inv.insertItem(i, acquired.createItemStack(), false);
				didAnything = true;
			}
		}

		return didAnything;
	}

	private Iterator<IAEItemStack> getIterator(IStorageGrid storageGrid) {
		return new ObjectArrayList<>(this.getStorageList(storageGrid).iterator()).stream().iterator();
	}

	@NotNull
	@Override
	public IGridNode getActionableNode() {
		return this.host.getActionableNode();
	}

	public IItemHandler getInternalInventory() {
		return this.inv;
	}

	@Override
	public void saveChanges() {
		this.host.markForSave();
	}

	@Override
	public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack,
	                              ItemStack itemStack1) {

	}

	public void getDrops(List<ItemStack> drops) {
		var inv = this.getInternalInventory();

		for (var l = 0; l < inv.getSlots(); ++l) {
			var is = inv.getStackInSlot(l);
			if (!is.isEmpty()) {
				drops.add(is);
			}
		}
	}

	public void writeToNBT(NBTTagCompound data) {
		this.inv.writeToNBT(data, "inv");
		this.cm.writeToNBT(data);
	}

	public void readFromNBT(NBTTagCompound data) {
		this.inv.readFromNBT(data, "inv");
		this.cm.readFromNBT(data);
	}

	@Override
	public void updateSetting(IConfigManager iConfigManager, Enum anEnum, Enum anEnum1) {
		this.host.markForSave();
	}

	public IConfigManager getConfigManager() {
		return this.cm;
	}

	public IItemHandler getInventoryByName(final String name) {
		if (name.equals("storage")) {
			return this.inv;
		}
		return null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades upgrades) {
		return 0;
	}

	@Override
	public TileEntity getTile() {
		if (this.host instanceof TileEntity te) {
			return te;
		}
		return null;
	}

	public boolean autoPush() {
		if (this.getConfigManager().getSetting(Settings.PLACE_BLOCK) == YesNo.NO) return false;

		var host = this.host.getTileEntity();
		var world = host.getWorld();

		var validSlots = new Int2ObjectOpenHashMap<ItemStack>();
		var mutated = new IntOpenHashSet();
		for (var slot = 0; slot < this.inv.getSlots(); slot++) {
			var stack = this.inv.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				validSlots.put(slot, stack);
			}
		}

		for (var target : this.targets) {
			if (validSlots.isEmpty()) {break;}

			var te = world.getTileEntity(host.getPos().offset(target));
			if (te == null) continue;
			var capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.getOpposite());
			if (capability == null) continue;

			var iterator = validSlots.int2ObjectEntrySet().fastIterator();
			while (iterator.hasNext()) {
				var pair = iterator.next();
				var stack = pair.getValue();
				var remaining = ItemHandlerHelper.insertItem(capability, stack, false);

				if (remaining.isEmpty() || remaining.getCount() != stack.getCount()) {
					var intKey = pair.getIntKey();

					if (!mutated.contains(intKey)) {
						mutated.add(intKey);
					}

					if (remaining.isEmpty()) {
						iterator.remove();
					} else {
						validSlots.put(intKey, remaining);
					}
				}
			}

		}

		for (var mutatedSlot : mutated) {
			var newStack = validSlots.getOrDefault(mutatedSlot, ItemStack.EMPTY);
			this.inv.setStackInSlot(mutatedSlot, newStack);
		}

		return !mutated.isEmpty();
	}

	public void setTargets(EnumSet<EnumFacing> targets) {
		this.targets = targets;
	}
}
