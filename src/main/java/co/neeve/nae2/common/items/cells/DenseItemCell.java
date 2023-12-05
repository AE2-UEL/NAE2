package co.neeve.nae2.common.items.cells;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import co.neeve.nae2.common.registration.definitions.Materials;
import org.jetbrains.annotations.NotNull;

public class DenseItemCell extends DenseCell<IAEItemStack> {
	public DenseItemCell(Materials.MaterialType whichCell, int kilobytes) {
		super(whichCell, kilobytes);
	}

	@NotNull
	@Override
	public IItemStorageChannel getChannel() {
		return Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class);
	}
}
