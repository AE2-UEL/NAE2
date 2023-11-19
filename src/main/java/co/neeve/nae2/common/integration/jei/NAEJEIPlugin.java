package co.neeve.nae2.common.integration.jei;

import appeng.api.implementations.items.IStorageCell;
import co.neeve.nae2.common.features.subfeatures.JEIFeatures;
import co.neeve.nae2.common.registries.Blocks;
import co.neeve.nae2.common.registries.InternalItems;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@JEIPlugin
@SideOnly(Side.CLIENT)
public class NAEJEIPlugin implements IModPlugin, IRecipeRegistryPlugin {
	@Override
	public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
		IModPlugin.super.registerCategories(registry);

		if (JEIFeatures.CELL_VIEW.isEnabled()) {
			IJeiHelpers jeiHelpers = registry.getJeiHelpers();

			registry.addRecipeCategories(new JEICellCategory(jeiHelpers));
		}
	}


	@Override
	public <V> @NotNull List<String> getRecipeCategoryUids(@NotNull IFocus<V> focus) {
		if (JEIFeatures.CELL_VIEW.isEnabled() && focus.getValue() instanceof ItemStack focusStack) {
			if (focusStack.getItem() instanceof IStorageCell<?>) {
				return Collections.singletonList(JEICellCategory.UID);
			}
		}

		return Collections.emptyList();
	}

	@Override
	public <T extends IRecipeWrapper, V> @NotNull List<T> getRecipeWrappers(@NotNull IRecipeCategory<T> recipeCategory,
	                                                                        @NotNull IFocus<V> focus) {
		if (JEIFeatures.CELL_VIEW.isEnabled() && recipeCategory instanceof JEICellCategory && focus.getValue() instanceof ItemStack is && is.getItem() instanceof IStorageCell<?>) {
			//noinspection unchecked
			return Collections.singletonList((T) new SingleStackRecipe(is));
		}
		return Collections.emptyList();
	}

	@Override
	public <T extends IRecipeWrapper> @NotNull List<T> getRecipeWrappers(@NotNull IRecipeCategory<T> recipeCategory) {
		if (JEIFeatures.CELL_VIEW.isEnabled() && recipeCategory instanceof JEICellCategory) {
			//noinspection unchecked
			return Collections.singletonList((T) new SingleStackRecipe(ItemStack.EMPTY));
		}
		return Collections.emptyList();
	}

	@Override
	public void register(@NotNull IModRegistry registry) {
		IModPlugin.super.register(registry);

		IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		for (InternalItems internalItem : InternalItems.values()) {
			blacklist.addIngredientToBlacklist(internalItem.getStack());
		}

		if (JEIFeatures.CELL_VIEW.isEnabled()) {
			registry.addRecipeRegistryPlugin(this);
		}

		for (var blockDef : Blocks.values()) {
			if (!blockDef.isEnabled()) continue;
			blockDef.jeiRegister(registry);
		}
	}
}
