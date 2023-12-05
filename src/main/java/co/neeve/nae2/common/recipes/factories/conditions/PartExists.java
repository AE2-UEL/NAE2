package co.neeve.nae2.common.recipes.factories.conditions;

import co.neeve.nae2.NAE2;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class PartExists implements IConditionFactory {

	private static final String JSON_MATERIAL_KEY = "name";

	@Override
	public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject) {
		final boolean result;

		if (JsonUtils.isString(jsonObject, JSON_MATERIAL_KEY)) {
			final var material = JsonUtils.getString(jsonObject, JSON_MATERIAL_KEY);
			var definition = NAE2.definitions().parts().getById(material).orElse(null);
			result = definition != null && definition.isEnabled();
		} else {
			result = false;
		}

		return () -> result;

	}
}