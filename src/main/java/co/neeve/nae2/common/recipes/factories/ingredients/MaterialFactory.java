package co.neeve.nae2.common.recipes.factories.ingredients;

import co.neeve.nae2.common.registries.Materials;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;

public class MaterialFactory implements IIngredientFactory {
	private static final String JSON_MATERIAL_KEY = "material";

	@Nonnull
	public Ingredient parse(JsonContext jsonContext, JsonObject jsonObject) {
		if (JsonUtils.isString(jsonObject, JSON_MATERIAL_KEY)) {
			final String part = JsonUtils.getString(jsonObject, JSON_MATERIAL_KEY);

			Materials materialDef;
			try {
				materialDef = Materials.valueOf(part.toUpperCase());
			} catch (IllegalArgumentException err) {
				materialDef = null;
			}

			if (materialDef != null && materialDef.isEnabled()) {
				return Ingredient.fromStacks(materialDef.getStack());
			}
		}
		return Ingredient.EMPTY;
	}
}
