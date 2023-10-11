package co.neeve.nae2.common.items;


import appeng.items.AEBaseItem;
import co.neeve.nae2.common.registries.Materials;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import org.jetbrains.annotations.NotNull;

public class NAEMaterial extends AEBaseItem {
	public static NAEMaterial instance;

	public NAEMaterial() {
		this.setHasSubtypes(true);

		instance = this;
	}

	public ItemStack getItemStack(Materials material) {
		return new ItemStack(this, 1, material.ordinal());
	}

	@Override
	public @NotNull String getTranslationKey(ItemStack itemStack) {
		return Materials.getByID(itemStack.getItemDamage()).getTranslationKey();
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		if (!this.isInCreativeTab(creativeTab)) return;

		for (Materials material : Materials.values()) {
			itemStacks.add(new ItemStack(this, 1, material.ordinal()));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal(Materials.getByID(stack.getItemDamage()).getTranslationKey());
	}
}
