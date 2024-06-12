package co.neeve.nae2.common.integration.jei.adapt;

import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import com.the9grounds.aeadditions.api.IAEAdditionsStorageCell;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AEACellAdapter<T extends IAEStack<T>> extends CellWorkbenchItemAdapter implements IStorageCell<T> {
	protected final IAEAdditionsStorageCell<T> cell;

	public AEACellAdapter(IAEAdditionsStorageCell<T> cell) {
		super(cell);
		this.cell = cell;
	}

	@Override
	public int getBytes(@NotNull ItemStack itemStack) {return this.cell.getBytes(itemStack);}

	@Override
	public int getBytesPerType(@NotNull ItemStack itemStack) {return this.cell.getBytesPerType(itemStack);}

	@Override
	public int getTotalTypes(@NotNull ItemStack itemStack) {return this.cell.getTotalTypes(itemStack);}

	@Override
	public boolean isBlackListed(@NotNull ItemStack itemStack, @NotNull T var2) {
		return this.cell.isBlackListed(itemStack, var2);
	}

	@Override
	public boolean storableInStorageCell() {return this.cell.storableInStorageCell();}

	@Override
	public boolean isStorageCell(@NotNull ItemStack itemStack) {return this.cell.isStorageCell(itemStack);}

	@Override
	public double getIdleDrain() {return this.cell.getIdleDrain();}

	@NotNull
	@Override
	public IStorageChannel<T> getChannel() {return this.cell.getChannel();}

}
