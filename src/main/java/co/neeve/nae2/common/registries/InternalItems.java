package co.neeve.nae2.common.registries;

import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.items.VirtualPattern;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public enum InternalItems {
	VIRTUAL_PATTERN("virtual_pattern", new VirtualPattern());

	private final Item item;
	private Features feature;

	InternalItems(String id, Item item) {
		this.item = item;
		this.item.setRegistryName(new ResourceLocation(Tags.MODID, id));
	}

	public Item getItem() {
		return this.item;
	}

	public ItemStack getStack() {
		return new ItemStack(item);
	}

	public boolean isEnabled() {
		return this.feature == null || feature.isEnabled();
	}
}
