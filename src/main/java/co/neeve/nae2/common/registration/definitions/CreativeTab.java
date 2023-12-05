package co.neeve.nae2.common.registration.definitions;

import co.neeve.nae2.NAE2;
import co.neeve.nae2.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeTab extends CreativeTabs {
	public static final CreativeTabs instance = new CreativeTab();

	public CreativeTab() {
		super(Tags.MODID);
	}

	@Override
	public @NotNull ItemStack createIcon() {
		return NAE2.icon();
	}
}
