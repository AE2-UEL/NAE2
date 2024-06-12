package co.neeve.nae2.common.integration.jei.adapt;

import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.data.IAEStack;
import com.the9grounds.aeadditions.api.IAEAdditionsStorageCell;

public class StorageCellWrapper {
	public static boolean isCell(Object obj) {
		return obj instanceof IStorageCell<?> || obj instanceof IAEAdditionsStorageCell;
	}

	@SuppressWarnings("unchecked")
	public static <T extends IAEStack<T>> IStorageCell<T> getCell(Object obj) {
		if (obj instanceof IStorageCell) {
			return (IStorageCell<T>) obj;
		}

		if (obj instanceof IAEAdditionsStorageCell) {
			return new AEACellAdapter<>((IAEAdditionsStorageCell<T>) obj);
		}

		return null;
	}
}
