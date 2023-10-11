package co.neeve.nae2.common.items.cells.vc;

import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import net.minecraft.item.ItemStack;

public final class VoidCellHandler implements ICellHandler {
	public VoidCellHandler() {
	}

	public boolean isCell(ItemStack is) {
		return is.getItem() instanceof StorageCellVoid || is.getItem() instanceof FluidStorageCellVoid;
	}

	@Override
	public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(ItemStack itemStack,
	                                                                         ISaveProvider iSaveProvider,
	                                                                         IStorageChannel<T> iStorageChannel) {
		return !itemStack.isEmpty()
			&& (
			(itemStack.getItem() instanceof StorageCellVoid && iStorageChannel instanceof IItemStorageChannel)
				|| (itemStack.getItem() instanceof FluidStorageCellVoid && iStorageChannel instanceof IFluidStorageChannel)
		) ? VoidCellInventory.getCell(itemStack) : null;
	}

	public int getStatusForCell(ItemStack is, ICellInventoryHandler handler) {
		return 2;
	}

	public double cellIdleDrain(ItemStack is, ICellInventoryHandler handler) {
		return 0.0;
	}
}
