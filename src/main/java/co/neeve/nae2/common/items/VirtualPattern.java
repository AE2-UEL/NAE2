package co.neeve.nae2.common.items;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import co.neeve.nae2.common.helpers.VirtualPatternDetails;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class VirtualPattern extends Item implements ICraftingPatternItem {
	@Override
	public ICraftingPatternDetails getPatternForItem(ItemStack itemStack, World world) {
		return VirtualPatternDetails.fromItemStack(itemStack);
	}
}
