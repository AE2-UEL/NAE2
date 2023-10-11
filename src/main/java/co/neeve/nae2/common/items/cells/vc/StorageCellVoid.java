package co.neeve.nae2.common.items.cells.vc;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;

public class StorageCellVoid extends BaseStorageCellVoid<IAEItemStack> {
	@Override
	public IStorageChannel<IAEItemStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
	}


}
