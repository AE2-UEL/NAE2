package co.neeve.nae2.common.recipes.factories.ingredients;

import co.neeve.nae2.common.registries.Parts;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;

public class PartFactory implements IIngredientFactory {
	private static final String JSON_MATERIAL_KEY = "part";

	@Nonnull
	public Ingredient parse(JsonContext jsonContext, JsonObject jsonObject) {
		if (JsonUtils.isString(jsonObject, JSON_MATERIAL_KEY)) {
			final String part = JsonUtils.getString(jsonObject, JSON_MATERIAL_KEY);

			Parts partDef;
			try {
				partDef = Parts.valueOf(part.toUpperCase());
			} catch (IllegalArgumentException err) {
				partDef = null;
			}

			if (partDef != null && partDef.isEnabled()) {
				return Ingredient.fromStacks(partDef.getStack());
			}
		}
		return Ingredient.EMPTY;
	}
}
