package co.neeve.nae2.mixin.jei.craft.shared;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import co.neeve.nae2.common.helpers.VirtualPatternDetails;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinCraftingCPUCluster {
	@Shadow
	private long remainingItemCount;

	@Shadow
	private IAEItemStack finalOutput;

	@Shadow
	public abstract void cancel();

	/**
	 * Virtual Patterns are considered done when the only remaining item to craft is the Pattern output.
	 */
	@Inject(method = "executeCrafting", at = @At(
		value = "INVOKE",
		target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;canCraft" +
			"(Lappeng/api/networking/crafting/ICraftingPatternDetails;[Lappeng/api/storage/data/IAEItemStack;)Z"
	), cancellable = true)
	public void executeCrafting(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci,
	                            @Local ICraftingPatternDetails details) {
		if (details instanceof VirtualPatternDetails) {
			if (this.remainingItemCount == this.finalOutput.getStackSize()) {
				this.cancel();
				ci.cancel();
			}
		}
	}
}
