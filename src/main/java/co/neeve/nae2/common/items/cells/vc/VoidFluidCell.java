package co.neeve.nae2.common.items.cells.vc;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;

public class VoidFluidCell extends VoidCell<IAEFluidStack> {
	@Override
	public IStorageChannel<IAEFluidStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
	}
}
