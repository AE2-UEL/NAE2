package co.neeve.nae2.mixin.patternmultitool.client;

import mezz.jei.gui.overlay.IngredientGrid;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Collection;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Pseudo
@Mixin(value = IngredientGrid.class, remap = false)
public class MixinJEIIngredientGrid {

	@Unique
	private Collection<Rectangle> rectangles = null;

	@Inject(
		at = @At("HEAD"),
		method = "shouldDeleteItemOnClick(Lnet/minecraft/client/Minecraft;II)Z",
		cancellable = true
	)
	private void shouldDeleteItemOnClick(Minecraft minecraft, int mouseX, int mouseY,
	                                     CallbackInfoReturnable<Boolean> cir) {
		if (this.rectangles != null) {
			if (this.rectangles.stream().anyMatch(x -> x.contains(mouseX, mouseY))) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "updateBounds(Ljava/awt/Rectangle;ILjava/util/Collection;)Z")
	public void updateBounds(Rectangle availableArea, int minWidth, Collection<Rectangle> exclusionAreas,
	                         CallbackInfoReturnable<Boolean> cir) {
		this.rectangles = exclusionAreas;
	}
}
