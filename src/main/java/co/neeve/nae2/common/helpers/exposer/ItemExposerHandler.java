package co.neeve.nae2.common.helpers.exposer;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import co.neeve.nae2.common.interfaces.IExposerHost;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemExposerHandler extends ExposerHandler<IAEItemStack> implements IItemHandler {
	private ObjectArrayList<IAEItemStack> itemCache;

	public ItemExposerHandler(IExposerHost host) {
		super(host);
	}

	@Override
	protected void onMonitorChange(IMEMonitor<IAEItemStack> oldValue, IMEMonitor<IAEItemStack> newValue) {
		this.updateItemCache();
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	protected IStorageChannel<IAEItemStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
	}

	@Override
	public void postChange(IBaseMonitor<IAEItemStack> iBaseMonitor, Iterable<IAEItemStack> iterable,
	                       IActionSource iActionSource) {
		this.updateItemCache();
	}

	@Override
	public void onListUpdate() {
		this.updateItemCache();
	}

	@Override
	public int getSlots() {
		this.updateMonitor();
		return this.itemCache != null ? this.itemCache.size() : 0;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		this.updateMonitor();

		if (this.itemCache != null && slot < this.itemCache.size()) {
			var stack = this.itemCache.get(slot);
			if (stack != null) {
				return stack.createItemStack();
			}
		}

		return ItemStack.EMPTY;
	}


	private void updateItemCache() {
		if (this.monitor == null) this.itemCache = null;
		else {
			this.itemCache = new ObjectArrayList<>();
			var storage = this.getStorage();
			if (storage != null) {
				for (var iaeItemStack : storage) {
					this.itemCache.add(iaeItemStack);
				}
			}
		}
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return stack;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		this.updateMonitor();
		var result = ItemStack.EMPTY;
		if (this.itemCache != null && slot < this.itemCache.size()) {
			var stack = this.itemCache.get(slot);
			if (stack != null) {
				try {
					var extracted = Platform.poweredExtraction(this.host.getProxy().getEnergy(),
						this.host.getProxy().getStorage().getInventory(this.getStorageChannel()),
						stack, this.mySrc, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
					if (extracted != null) {
						result = extracted.createItemStack();
					}
				} catch (GridAccessException ignored) {}
			}
		}

		return result;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Integer.MAX_VALUE;
	}
}
