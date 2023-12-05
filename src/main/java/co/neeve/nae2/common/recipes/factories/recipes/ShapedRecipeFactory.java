package co.neeve.nae2.common.recipes.factories.recipes;

import co.neeve.nae2.NAE2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ShapedRecipeFactory implements IRecipeFactory {
	@Override
	public ShapedOreRecipe parse(JsonContext context, JsonObject json) {
		var group = JsonUtils.getString(json, "group", "");

		Map<Character, Ingredient> ingMap = Maps.newHashMap();
		for (var entry : JsonUtils.getJsonObject(json, "key").entrySet()) {
			if (entry.getKey().length() != 1)
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol " +
					"(must" +
					" " +
					"be 1 character only).");
			if (" ".equals(entry.getKey()))
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

			ingMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
		}

		ingMap.put(' ', Ingredient.EMPTY);

		var patternJ = JsonUtils.getJsonArray(json, "pattern");

		if (patternJ.size() == 0)
			throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");

		var pattern = new String[patternJ.size()];
		for (var x = 0; x < pattern.length; ++x) {
			var line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
			if (x > 0 && pattern[0].length() != line.length())
				throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
			pattern[x] = line;
		}

		var primer = new CraftingHelper.ShapedPrimer();
		primer.width = pattern[0].length();
		primer.height = pattern.length;
		primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
		primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);

		Set<Character> keys = Sets.newHashSet(ingMap.keySet());
		keys.remove(' ');

		var x = 0;
		for (var line : pattern) {
			for (var chr : line.toCharArray()) {
				var ing = ingMap.get(chr);
				if (ing == null)
					throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in " +
						"the" +
						" key");
				primer.input.set(x++, ing);
				keys.remove(chr);
			}
		}

		if (!keys.isEmpty())
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

		var result = JsonUtils.getJsonObject(json, "result");
		var type = result.get("type").getAsString();
		var name = result.get("name").getAsString();
		var count = result.has("count") ? result.get("count").getAsInt() : 1;

		ItemStack resultStack;
		if (Objects.equals(type, "part")) {
			var partDef = NAE2.definitions().parts().getById(name).orElse(null);

			if (partDef == null || !partDef.isEnabled())
				throw new IllegalStateException("Part \"" + name + "\" doesn't exist or is disabled");

			resultStack = partDef.maybeStack(1).orElseThrow(() ->
				new IllegalStateException("Material \"" + name + "\" doesn't exist or is disabled"));
		} else if (Objects.equals(type, "material")) {
			var materialDef = NAE2.definitions().materials().getById(name).orElse(null);

			if (materialDef == null || !materialDef.isEnabled())
				throw new IllegalStateException("Material \"" + name + "\" doesn't exist or is disabled");

			resultStack = materialDef.maybeStack(1).orElseThrow(() ->
				new IllegalStateException("Material \"" + name + "\" doesn't exist or is disabled"));
		} else {
			throw new IllegalStateException("Unexpected output type \"" + type + "\"");
		}

		resultStack.setCount(count);
		return new ShapedOreRecipe(group.isEmpty() ? null : new ResourceLocation(group), resultStack, primer);
	}
}
