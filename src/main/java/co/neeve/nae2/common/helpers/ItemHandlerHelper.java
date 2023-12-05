package co.neeve.nae2.common.helpers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerHelper {
	public static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack) {
		for (var i = 0; i < handler.getSlots(); i++) {
			var remaining = handler.insertItem(i, stack, false);
			if (remaining.isEmpty()) {
				return remaining; // All items were inserted.
			}
			stack = remaining; // Not all items were inserted; try the next slot.
		}
		return stack;
	}
}
