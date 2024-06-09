package co.neeve.nae2.mixin.core.crafting.patterntransform;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.me.cache.CraftingGridCache;
import co.neeve.nae2.common.crafting.patterntransform.PatternTransform;
import co.neeve.nae2.common.crafting.patterntransform.PatternTransformWrapper;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingGridCache.class, remap = false)
public class MixinCraftingGrid {
	@Inject(method = "addCraftingOption", at = @At("HEAD"))
	private void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails api, CallbackInfo ci,
	                               @Local LocalRef<ICraftingPatternDetails> detailsLocalRef) {
		if (PatternTransform.transform(medium, api) instanceof PatternTransformWrapper wrapper) {
			detailsLocalRef.set(wrapper);
		}
	}
}
