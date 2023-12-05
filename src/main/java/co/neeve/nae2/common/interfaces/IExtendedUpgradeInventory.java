package co.neeve.nae2.common.interfaces;

import co.neeve.nae2.common.registration.definitions.Upgrades;

public interface IExtendedUpgradeInventory {
	int getInstalledUpgrades(Upgrades.UpgradeType u);

	int getMaxInstalled(Upgrades.UpgradeType u);
}
