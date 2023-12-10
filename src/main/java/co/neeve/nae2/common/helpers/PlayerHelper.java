package co.neeve.nae2.common.helpers;

import baubles.api.BaublesApi;
import co.neeve.nae2.common.items.patternmultitool.ToolPatternMultiTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerHelper {
	public static @Nullable ItemStack getPatternMultiTool(EntityPlayer ep) {
		final List<IInventory> inventories = new ArrayList<>();
		try {
			inventories.add(getBaubles(ep));
		} catch (NoSuchMethodError ignored) {}

		inventories.add(ep.inventory);

		for (var inv : inventories) {
			var pii = getPMTStack(inv);
			if (pii == null) continue;
			return pii;
		}
		return null;
	}

	@Nullable
	private static ItemStack getPMTStack(IInventory ip) {
		for (var v = 0; v < ip.getSizeInventory(); ++v) {
			var pii = ip.getStackInSlot(v);
			if (!pii.isEmpty() && pii.getItem() instanceof ToolPatternMultiTool) {
				return pii;
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Optional.Method(modid = "baubles")
	public static IInventory getBaubles(EntityPlayer ep) throws NoSuchMethodError {
		return BaublesApi.getBaubles(ep);
	}

	public static int getSlotFor(InventoryPlayer inventoryPlayer, ItemStack patternMultiTool) {
		for (var i = 0; i < inventoryPlayer.mainInventory.size(); ++i) {
			if (!inventoryPlayer.mainInventory.get(i).isEmpty()) {
				var stack2 = inventoryPlayer.mainInventory.get(i);
				if (patternMultiTool.getItem() == stack2.getItem() && (!patternMultiTool.getHasSubtypes() || patternMultiTool.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(
					patternMultiTool,
					stack2)) {
					return i;
				}
			}
		}

		return -1;
	}
}
