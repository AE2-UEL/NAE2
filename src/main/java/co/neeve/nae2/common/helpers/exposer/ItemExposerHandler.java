package co.neeve.nae2.common.helpers.exposer;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import co.neeve.nae2.common.interfaces.IExposerHost;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ItemExposerHandler extends AEStackExposerHandler<IAEItemStack> implements IItemHandler {
	private static final IItemStorageChannel CHANNEL =
		AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);

	public ItemExposerHandler(IExposerHost host) {
		super(host);
	}

	@Override
	protected IStorageChannel<IAEItemStack> getStorageChannel() {
		return CHANNEL;
	}

	@Override
	public int getSlots() {
		this.updateMonitor();
		return this.getCache().size();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		var stack = this.getInSlot(slot);
		return stack == null ? ItemStack.EMPTY : stack.createItemStack();
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return stack;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		var pulled = this.pullStackFromSlot(slot, amount, simulate);
		return pulled == null ? ItemStack.EMPTY : pulled.createItemStack();
	}

	@Override
	public int getSlotLimit(int slot) {
		return Integer.MAX_VALUE;
	}
}
