package co.neeve.nae2.common.registries;

import co.neeve.nae2.Tags;
import co.neeve.nae2.items.patternmultitool.ToolPatternMultiTool;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public enum Items {
	PATTERN_MULTI_TOOL("pattern_multiplier", new ToolPatternMultiTool());

	private final Item item;
	private final String id;

	Items(String id, Item item) {
		this.item = item;
		this.id = id;
		this.item.setTranslationKey(Tags.MODID + "." + this.id);
		this.item.setRegistryName(new ResourceLocation(Tags.MODID, this.id));
	}

	public Item getItem() {
		return this.item;
	}

	public String getId() {
		return this.id;
	}
}
