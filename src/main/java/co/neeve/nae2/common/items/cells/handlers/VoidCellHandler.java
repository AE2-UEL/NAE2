package co.neeve.nae2.common.items.cells.handlers;

import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import co.neeve.nae2.common.interfaces.IVoidingCellHandler;
import co.neeve.nae2.common.items.cells.vc.VoidCell;
import co.neeve.nae2.common.items.cells.vc.VoidCellInventory;
import co.neeve.nae2.common.items.cells.vc.VoidFluidCell;
import co.neeve.nae2.common.items.cells.vc.VoidItemCell;
import net.minecraft.item.ItemStack;

public final class VoidCellHandler implements IVoidingCellHandler {
	public VoidCellHandler() {
	}

	@Override
	public boolean isCell(ItemStack is) {
		return is.getItem() instanceof VoidCell;
	}

	@Override
	public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(ItemStack itemStack,
	                                                                         ISaveProvider iSaveProvider,
	                                                                         IStorageChannel<T> iStorageChannel) {
		return !itemStack.isEmpty()
			&& (
			(itemStack.getItem() instanceof VoidItemCell && iStorageChannel instanceof IItemStorageChannel)
				|| (itemStack.getItem() instanceof VoidFluidCell && iStorageChannel instanceof IFluidStorageChannel)
		) ? new VoidCellInventory<>(itemStack, iSaveProvider) : null;
	}

	@Override
	public int getStatusForCell(ItemStack is, ICellInventoryHandler handler) {
		return 2;
	}

	@Override
	public double cellIdleDrain(ItemStack is, ICellInventoryHandler handler) {
		return 2.0;
	}
}
