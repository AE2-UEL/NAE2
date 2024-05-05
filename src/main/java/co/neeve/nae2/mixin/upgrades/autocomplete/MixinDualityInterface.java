package co.neeve.nae2.mixin.upgrades.autocomplete;

import appeng.helpers.DualityInterface;
import appeng.parts.automation.UpgradeInventory;
import co.neeve.nae2.common.interfaces.ICancellingCraftingMedium;
import co.neeve.nae2.common.interfaces.IExtendedUpgradeInventory;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DualityInterface.class, remap = false)
public class MixinDualityInterface implements ICancellingCraftingMedium {
	@Shadow
	@Final
	private UpgradeInventory upgrades;

	@Override
	public boolean shouldAutoComplete() {
		return this.upgrades instanceof IExtendedUpgradeInventory upgradeInventory
			&& upgradeInventory.getInstalledUpgrades(Upgrades.UpgradeType.AUTO_COMPLETE) > 0;
	}
}
