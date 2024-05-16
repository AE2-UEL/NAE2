package co.neeve.nae2.mixin.ifacep2p.shared;

import appeng.helpers.DualityInterface;
import org.spongepowered.asm.mixin.Mixin;

/**
 * isBusy stuff.
 * Instead of looping over just the six sides, we check if the entity being looked at is a tunnel.
 * If it is, we hijack the entire thing.
 * This virtually doesn't affect the default behavior.
 */
@SuppressWarnings("rawtypes")
@Mixin(value = DualityInterface.class, remap = false)
public abstract class MixinTermName {

}
