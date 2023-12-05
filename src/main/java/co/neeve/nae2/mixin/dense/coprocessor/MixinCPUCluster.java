package co.neeve.nae2.mixin.dense.coprocessor;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import co.neeve.nae2.common.interfaces.IDenseCoProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinCPUCluster {
	@Shadow
	private int accelerator;

	@Inject(method = "addTile", at = @At(
		"RETURN"
	))
	public void addTile(TileCraftingTile te, CallbackInfo ci) {
		if (te instanceof IDenseCoProcessor denseCoProcessor) {
			this.accelerator += denseCoProcessor.getAccelerationFactor();
		}
	}
}
