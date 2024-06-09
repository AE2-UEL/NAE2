package co.neeve.nae2.common.api;

import appeng.util.item.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;

public class TunnelConversionAPI {
	private final Object2ObjectMap<ItemStack, ItemStack> tunnelMap =
		new Object2ObjectOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());

	public void register(ItemStack catalyst, ItemStack tunnelStack) {
		this.tunnelMap.put(catalyst, tunnelStack);
	}

	public ItemStack getConversion(ItemStack catalyst) {
		return this.tunnelMap.getOrDefault(catalyst, ItemStack.EMPTY);
	}
}
