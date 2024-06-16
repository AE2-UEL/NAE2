package co.neeve.nae2.common.helpers.inv;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemHandlerDelegate implements IItemHandler, IDelegate<IItemHandler> {
	@Nullable
	private IItemHandler delegate;

	public ItemHandlerDelegate(@Nullable IItemHandler delegate) {
		this.delegate = delegate;
	}

	public ItemHandlerDelegate() {
		this(null);
	}

	@Override
	public int getSlots() {
		if (this.delegate == null) {
			return 0;
		}

		return this.delegate.getSlots();
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {return this.delegate.getStackInSlot(slot);}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (this.delegate == null) {
			return stack;
		}

		return this.delegate.insertItem(slot, stack, simulate);
	}

	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (this.delegate == null) {
			return ItemStack.EMPTY;
		}

		return this.delegate.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		if (this.delegate == null) {
			return 0;
		}

		return this.delegate.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		if (this.delegate == null) {
			return false;
		}

		return this.delegate.isItemValid(slot, stack);
	}

	@Nullable
	public IItemHandler getDelegate() {
		return this.delegate;
	}

	public void setDelegate(@Nullable IItemHandler delegate) {
		this.delegate = delegate;
	}
}
