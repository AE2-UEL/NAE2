package co.neeve.nae2.common.items.cells;

import appeng.core.Api;
import co.neeve.nae2.common.registration.definitions.Materials;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DenseGasCell extends DenseCell<IAEGasStack> {
	public DenseGasCell(Materials.MaterialType whichCell, int kilobytes) {
		super(whichCell, kilobytes);
	}

	@NotNull
	@Override
	public IGasStorageChannel getChannel() {
		return Api.INSTANCE.storage().getStorageChannel(IGasStorageChannel.class);
	}

	@Override
	public int getTotalTypes(@NotNull ItemStack cellItem) {
		return 15;
	}
}
