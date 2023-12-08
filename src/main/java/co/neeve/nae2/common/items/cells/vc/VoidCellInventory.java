package co.neeve.nae2.common.items.cells.vc;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.helper.FluidCellConfig;
import appeng.fluids.items.FluidDummyItem;
import appeng.fluids.util.AEFluidStack;
import appeng.items.contents.CellConfig;
import appeng.me.storage.BasicCellInventoryHandler;
import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;

public class VoidCellInventory<T extends IAEStack<T>> implements IMEInventoryHandler<T> {
	private final IStorageChannel<T> channel;
	private final ItemStack itemStack;
	private final IItemList<T> itemListCache;
	private final BaseStorageCellVoid<T> item;

	@SuppressWarnings("unchecked")
	protected VoidCellInventory(ItemStack o) {
		this.itemStack = o;

		this.item = (BaseStorageCellVoid<T>) o.getItem();
		this.channel = this.item.getStorageChannel();
		this.itemListCache = this.getChannel().createList();

		var cc = this.isFluid() ? new FluidCellConfig(o) : new CellConfig(o);
		for (final var is : cc) {
			if (!is.isEmpty()) {
				if (this.isFluid() && is.getItem() instanceof FluidDummyItem fdi) {
					this.itemListCache.add((T) AEFluidStack.fromFluidStack(fdi.getFluidStack(is)));
				} else if (!this.isFluid()) {
					this.itemListCache.add((T) AEItemStack.fromItemStack(is));
				}
			}
		}
	}

	public static <T extends IAEStack<T>> ICellInventoryHandler<T> getCell(final ItemStack o) {
		var inv = new VoidCellInventory<T>(o);
		return new BasicCellInventoryHandler<>(inv, inv.getChannel());
	}

	public T injectItems(T input, Actionable mode, IActionSource src) {
		if (!this.itemListCache.isEmpty() && this.itemListCache.findPrecise(input) == null) return input;

		if (mode == Actionable.MODULATE) {
			this.item.addCondenserPowerFromInput(this.itemStack, input.getStackSize());
		}

		return null;
	}

	public T extractItems(T request, Actionable mode, IActionSource src) {
		return null;
	}

	public IItemList<T> getAvailableItems(IItemList<T> out) {
		return out;
	}

	@Override
	public IStorageChannel<T> getChannel() {
		return this.channel;
	}

	private boolean isFluid() {
		return this.itemStack.getItem() instanceof FluidStorageCellVoid;
	}

	public AccessRestriction getAccess() {
		return AccessRestriction.WRITE;
	}

	public boolean isPrioritized(T input) {
		return this.itemListCache.isEmpty() || this.itemListCache.findPrecise(input) != null;
	}

	public boolean canAccept(T input) {
		return this.itemListCache.isEmpty() || this.itemListCache.findPrecise(input) != null;
	}

	public int getPriority() {
		return 0;
	}

	public int getSlot() {
		return 0;
	}

	public boolean validForPass(int i) {
		return true;
	}
}
