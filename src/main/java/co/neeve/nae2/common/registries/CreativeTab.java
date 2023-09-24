package co.neeve.nae2.common.registries;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeTab extends CreativeTabs {
	public static final CreativeTabs instance = new CreativeTab();

	public CreativeTab() {
		super("nae2");
	}

	@Override
	public @NotNull ItemStack createIcon() {
		return new ItemStack(Items.PATTERN_MULTI_TOOL.getItem(), 1);
	}
}
