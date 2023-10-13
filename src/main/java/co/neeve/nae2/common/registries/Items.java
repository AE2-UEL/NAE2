package co.neeve.nae2.common.registries;

import co.neeve.nae2.Tags;
import co.neeve.nae2.common.items.NAEBaseItemPart;
import co.neeve.nae2.common.items.NAEBaseItemUpgrade;
import co.neeve.nae2.common.items.NAEMaterial;
import co.neeve.nae2.common.items.cells.vc.FluidStorageCellVoid;
import co.neeve.nae2.common.items.cells.vc.StorageCellVoid;
import co.neeve.nae2.common.items.patternmultitool.ToolPatternMultiTool;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public enum Items {
	PATTERN_MULTI_TOOL("pattern_multiplier", new ToolPatternMultiTool()),
	BASE_PART("part", new NAEBaseItemPart()),
	BASE_UPGRADE("upgrade", new NAEBaseItemUpgrade()),
	MATERIAL("material", new NAEMaterial()),
	STORAGE_CELL_VOID("storage_cell_void", new StorageCellVoid()),
	FLUID_STORAGE_CELL_VOID("fluid_storage_cell_void", new FluidStorageCellVoid());

	private final Item item;

	Items(String id, Item item) {
		this.item = item;
		this.item.setTranslationKey(Tags.MODID + "." + id);
		this.item.setRegistryName(new ResourceLocation(Tags.MODID, id));
		this.item.setCreativeTab(CreativeTab.instance);
	}

	public Item getItem() {
		return this.item;
	}

	public ItemStack getStack() {
		return new ItemStack(item);
	}
}
