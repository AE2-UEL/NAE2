package co.neeve.nae2.common.items.cells.vc;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.helper.FluidCellConfig;
import appeng.fluids.items.FluidDummyItem;
import appeng.fluids.util.AEFluidStack;
import appeng.items.contents.CellConfig;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VoidFluidCell extends VoidCell<IAEFluidStack> {
	@Override
	public IStorageChannel<IAEFluidStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
	}

	@Override
	public CellConfig getCellConfig(ItemStack o) {
		return new FluidCellConfig(o);
	}

	@Override
	@Nullable
	public IAEFluidStack handleConfigStack(ItemStack stack) {
		if (stack.getItem() instanceof FluidDummyItem fdi) {
			return AEFluidStack.fromFluidStack(fdi.getFluidStack(stack));
		}

		return null;
	}
}
