package co.neeve.nae2.mixin.upgrades.hac;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.storage.TileIOPort;
import co.neeve.nae2.common.interfaces.IExtendedUpgradeInventory;
import co.neeve.nae2.common.interfaces.INAEUpgradeHost;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileIOPort.class, remap = false)
public class MixinTileIOPort implements INAEUpgradeHost {
	@Shadow
	@Final
	private UpgradeInventory upgrades;

	@Inject(method = "tickingRequest", at = @At(
		value = "INVOKE",
		target = "Lappeng/tile/storage/TileIOPort;getInstalledUpgrades(Lappeng/api/config/Upgrades;)I"
	))
	private void injectTickingRequest(IGridNode node, int ticksSinceLastCall,
	                                  CallbackInfoReturnable<TickRateModulation> cir,
	                                  @Local LocalLongRef itemsToSend) {
		var hyper =
			this.getInstalledUpgrades(co.neeve.nae2.common.registration.definitions.Upgrades.UpgradeType.HYPER_ACCELERATION);

		if (hyper > 0) {
			itemsToSend.set((long) (itemsToSend.get() * 16 * Math.pow(8, hyper - 1)));
		}
	}

	@Override
	public int getInstalledUpgrades(Upgrades.UpgradeType u) {
		return ((IExtendedUpgradeInventory) this.upgrades).getInstalledUpgrades(u);
	}
}
