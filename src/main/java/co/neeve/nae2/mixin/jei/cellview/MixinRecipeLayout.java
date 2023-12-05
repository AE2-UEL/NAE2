package co.neeve.nae2.mixin.jei.cellview;

import co.neeve.nae2.common.integration.jei.IRecipeCategoryWithOverlay;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeLayout.class, remap = false)
public class MixinRecipeLayout {
	@Shadow
	@Final
	private IRecipeCategory<?> recipeCategory;

	@Shadow
	private int posY;

	@Shadow
	private int posX;

	@Inject(method = "drawRecipe", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/GlStateManager;disableBlend()V",
		remap = true
	))
	public void drawOverlay(Minecraft minecraft, int mouseX, int mouseY, CallbackInfo ci) {
		if (this.recipeCategory instanceof IRecipeCategoryWithOverlay withOverlay) {
			withOverlay.drawOverlay(minecraft, this.posX, this.posY, mouseX, mouseY);
		}
	}
}
