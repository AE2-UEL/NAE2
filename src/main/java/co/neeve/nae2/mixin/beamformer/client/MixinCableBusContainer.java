package co.neeve.nae2.mixin.beamformer.client;

import appeng.parts.CableBusContainer;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CableBusContainer.class)
public abstract class MixinCableBusContainer implements IBeamFormerHost {

	@Unique
	private boolean nae2$hasBeamFormers = false;

	@Override
	public void notifyBeamFormerState() {
		this.nae2$hasBeamFormers = true;
	}

	@Override
	public boolean hasBeamFormers() {
		return this.nae2$hasBeamFormers;
	}

	@Inject(method = "partChanged", at = @At("HEAD"), remap = false)
	public void injectPartChanged(CallbackInfo ci) {
		this.nae2$hasBeamFormers = false;
	}
}
