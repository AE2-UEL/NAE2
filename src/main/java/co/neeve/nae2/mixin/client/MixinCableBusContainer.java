package co.neeve.nae2.mixin.client;

import appeng.parts.CableBusContainer;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CableBusContainer.class)
public abstract class MixinCableBusContainer implements IBeamFormerHost {

	private boolean hasBeamFormers = false;

	@Override
	public void notifyBeamFormerState() {
		this.hasBeamFormers = true;
	}

	@Override
	public boolean hasBeamFormers() {
		return this.hasBeamFormers;
	}

	@Inject(method = "partChanged", at = @At("HEAD"), remap = false)
	public void injectPartChanged(CallbackInfo ci) {
		this.hasBeamFormers = false;
	}
}
