package co.neeve.nae2.common.recipes.factories.ingredients;

import co.neeve.nae2.NAE2;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;

public class PartFactory implements IIngredientFactory {
	private static final String JSON_MATERIAL_KEY = "name";

	@Nonnull
	public Ingredient parse(JsonContext jsonContext, JsonObject jsonObject) {
		if (JsonUtils.isString(jsonObject, JSON_MATERIAL_KEY)) {
			final var material = JsonUtils.getString(jsonObject, JSON_MATERIAL_KEY);
			var definition = NAE2.definitions().parts().getById(material).orElse(null);
			if (definition != null) {
				return Ingredient.fromStacks(definition.maybeStack(1).orElse(ItemStack.EMPTY));
			}
		}
		return Ingredient.EMPTY;
	}
}
