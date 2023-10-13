package co.neeve.nae2.common.recipes.factories.conditions;

import co.neeve.nae2.common.registries.Materials;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class MaterialExists implements IConditionFactory {

	private static final String JSON_MATERIAL_KEY = "material";

	@Override
	public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject) {
		boolean result = false;

		if (JsonUtils.isString(jsonObject, JSON_MATERIAL_KEY)) {
			final String name = JsonUtils.getString(jsonObject, JSON_MATERIAL_KEY);

			Materials materialDef;
			try {
				materialDef = Materials.valueOf(name.toUpperCase());

				result = materialDef.isEnabled();
			} catch (IllegalArgumentException ignored) {}
		}
		boolean finalResult = result;
		return () -> finalResult;
	}
}