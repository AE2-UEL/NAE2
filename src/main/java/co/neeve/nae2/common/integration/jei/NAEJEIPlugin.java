package co.neeve.nae2.common.integration.jei;

import appeng.api.definitions.IItemDefinition;
import appeng.api.implementations.items.IStorageCell;
import co.neeve.nae2.common.features.subfeatures.JEIFeatures;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JEIPlugin
@SideOnly(Side.CLIENT)
public class NAEJEIPlugin implements IModPlugin, IRecipeRegistryPlugin {
	private static final Object2ObjectOpenHashMap<IItemDefinition, String[]> ingredientDescriptions =
		new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<IItemDefinition, String[]> ingredientCatalysts =
		new Object2ObjectOpenHashMap<>();

	private static final ObjectOpenHashSet<IItemDefinition> ingredientBlacklist = new ObjectOpenHashSet<>();

	public static void registerDescription(IItemDefinition ingredient, String... descriptions) {
		ingredientDescriptions.put(ingredient, descriptions);
	}

	public static void registerCatalyst(IItemDefinition ingredient, String... descriptions) {
		ingredientCatalysts.put(ingredient, descriptions);
	}

	public static void registerBlacklist(IItemDefinition ingredient) {
		ingredientBlacklist.add(ingredient);
	}


	@Override
	public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
		IModPlugin.super.registerCategories(registry);

		if (JEIFeatures.CELL_VIEW.isEnabled()) {
			var jeiHelpers = registry.getJeiHelpers();

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

		if (JEIFeatures.CELL_VIEW.isEnabled()) {
			registry.addRecipeRegistryPlugin(this);
		}

		for (var descEntry : ingredientDescriptions.object2ObjectEntrySet()) {
			descEntry.getKey().maybeStack(1).ifPresent(itemStack ->
				registry.addIngredientInfo(itemStack, VanillaTypes.ITEM,
					Arrays.stream(descEntry.getValue()).map(I18n::format).toArray(String[]::new)));
		}

		for (var catalystEntry : ingredientCatalysts.object2ObjectEntrySet()) {
			catalystEntry.getKey().maybeStack(1).ifPresent(itemStack ->
				registry.addRecipeCatalyst(itemStack,
					Arrays.stream(catalystEntry.getValue()).map(I18n::format).toArray(String[]::new)));
		}

		var blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		for (var blacklistEntry : ingredientBlacklist) {
			blacklistEntry.maybeStack(1).ifPresent(blacklist::addIngredientToBlacklist);
		}
	}
}
