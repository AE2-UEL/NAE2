package co.neeve.nae2.common.registries;

import co.neeve.nae2.Tags;
import co.neeve.nae2.common.items.NAEBaseItemPart;
import co.neeve.nae2.common.items.patternmultitool.ToolPatternMultiTool;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public enum Items {
	PATTERN_MULTI_TOOL("pattern_multiplier", new ToolPatternMultiTool()),
	BASE_PART("part", new NAEBaseItemPart());

	private final Item item;
	private final String id;

	Items(String id, Item item) {
		this.item = item;
		this.id = id;
		this.item.setTranslationKey(Tags.MODID + "." + this.id);
		this.item.setRegistryName(new ResourceLocation(Tags.MODID, this.id));
		this.item.setCreativeTab(CreativeTab.instance);
	}

	public Item getItem() {
		return this.item;
	}

	public String getId() {
		return this.id;
	}
}
