package co.neeve.nae2.mixin.beamformer.client;

import appeng.parts.CableBusContainer;
import appeng.tile.networking.TileCableBus;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileCableBus.class)
public abstract class MixinTileCableBus {
	@Shadow(remap = false)
	public abstract CableBusContainer getCableBus();

	@Inject(method = "getMaxRenderDistanceSquared", at = @At("HEAD"), cancellable = true)
	private void injectRenderDistance(CallbackInfoReturnable<Double> cir) {
		if (this.getCableBus() instanceof IBeamFormerHost bh && bh.hasBeamFormers()) {
			cir.setReturnValue(Double.MAX_VALUE);
		}
	}
}
