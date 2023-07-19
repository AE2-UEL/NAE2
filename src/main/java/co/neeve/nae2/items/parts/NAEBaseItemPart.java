package co.neeve.nae2.items.parts;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.items.AEBaseItem;
import co.neeve.nae2.common.registries.Parts;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("rawtypes")
public class NAEBaseItemPart extends AEBaseItem implements IPartItem {
	public static NAEBaseItemPart instance;

	public NAEBaseItemPart() {
		this.setHasSubtypes(true);

		instance = this;
	}

	@Override
	public @NotNull String getTranslationKey(ItemStack itemStack) {
		return Parts.getByID(itemStack.getItemDamage()).getTranslationKey();
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		if (!this.isInCreativeTab(creativeTab)) return;

		for (Parts part : Parts.values()) {
			itemStacks.add(new ItemStack(this, 1, part.ordinal()));
		}
	}

	@Nullable
	@Override
	public IPart createPartFromItemStack(ItemStack is) {
		try {
			return Parts.values()[MathHelper.clamp(
				is.getItemDamage(), 0, Parts.values().length - 1)]
				.newInstance(is);
		} catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
		         InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}