package co.neeve.nae2.common.integration.jei;

import com.github.bsideup.jabel.Desugar;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@Desugar
@SideOnly(Side.CLIENT)
public record SingleStackRecipe(ItemStack stack) implements IRecipeWrapper {
	@Override
	public void getIngredients(@NotNull IIngredients ingredients) {
		ingredients.setInput(VanillaTypes.ITEM, this.stack);
		ingredients.setOutput(VanillaTypes.ITEM, this.stack);
	}
}
