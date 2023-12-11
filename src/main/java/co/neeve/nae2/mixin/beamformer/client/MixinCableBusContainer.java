package co.neeve.nae2.mixin.beamformer.client;

import appeng.api.parts.IPart;
import appeng.parts.CableBusContainer;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import co.neeve.nae2.common.parts.implementations.PartBeamFormer;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CableBusContainer.class)
public abstract class MixinCableBusContainer implements IBeamFormerHost {
	@Unique
	private boolean nae2$hasBeamFormers = false;

	@Shadow(remap = false)
	public abstract IPart getPart(EnumFacing side);

	@Override
	public boolean hasBeamFormers() {
		return this.nae2$hasBeamFormers;
	}

	@Inject(method = "partChanged", at = @At("HEAD"), remap = false)
	public void injectPartChanged(CallbackInfo ci) {
		this.nae2$hasBeamFormers = false;
		for (var facing : EnumFacing.values()) {
			var part = this.getPart(facing);
			if (part instanceof PartBeamFormer) {
				this.nae2$hasBeamFormers = true;
				break;
			}
		}
	}
}
