package co.neeve.nae2.mixin.ifacep2p.shared;

import appeng.api.networking.IGrid;
import appeng.me.cache.P2PCache;
import co.neeve.nae2.common.parts.p2p.iface.InterfaceTunnelGridCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = P2PCache.class, remap = false)
public class MixinP2PCache {
	@Shadow
	@Final
	private IGrid myGrid;

	@Inject(method = "updateTunnel", at = @At("RETURN"))
	private void onTunnelUpdate(short freq, boolean updateOutputs, boolean configChange, CallbackInfo ci) {
		var ifaceCache = (InterfaceTunnelGridCache) this.myGrid.getCache(InterfaceTunnelGridCache.class);
		ifaceCache.updateTunnelNetwork(freq);
	}
}
