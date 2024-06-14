package co.neeve.nae2.common.items.cells.vc;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.contents.CellConfig;
import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;

public class VoidItemCell extends VoidCell<IAEItemStack> {
	@Override
	public IStorageChannel<IAEItemStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
	}

	@Override
	public CellConfig getCellConfig(ItemStack o) {
		return new CellConfig(o);
	}

	@Override
	public IAEItemStack handleConfigStack(ItemStack stack) {
		return AEItemStack.fromItemStack(stack);
	}
}
