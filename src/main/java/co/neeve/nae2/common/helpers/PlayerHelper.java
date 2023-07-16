package co.neeve.nae2.common.helpers;

import baubles.api.BaublesApi;
import co.neeve.nae2.items.patternmultitool.ToolPatternMultiTool;
import net.minecraft.entity.player.EntityPlayer;
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

		for (IInventory inv : inventories) {
			ItemStack pii = getPMTStack(inv);
			if (pii == null) continue;
			return pii;
		}
		return null;
	}

	@Nullable
	private static ItemStack getPMTStack(IInventory ip) {
		for (int v = 0; v < ip.getSizeInventory(); ++v) {
			ItemStack pii = ip.getStackInSlot(v);
			if (!pii.isEmpty() && pii.getItem() instanceof ToolPatternMultiTool) {
				return pii;
			}
		}
		return null;
	}

	@Optional.Method(modid = "baubles")
	public static IInventory getBaubles(EntityPlayer ep) throws NoSuchMethodError {
		return BaublesApi.getBaubles(ep);
	}
}
