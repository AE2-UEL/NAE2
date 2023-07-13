package co.neeve.nae2.common.helpers;

import co.neeve.nae2.items.patternmultitool.ToolPatternMultiTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class PlayerHelper {
	public static @Nullable ItemStack getPatternMultiTool(EntityPlayer ep) {
		InventoryPlayer ip = ep.inventory;
		for (int v = 0; v < ip.getSizeInventory(); ++v) {
			ItemStack pii = ip.getStackInSlot(v);
			if (!pii.isEmpty() && pii.getItem() instanceof ToolPatternMultiTool) {
				return pii;
			}
		}

		return null;
	}
}
