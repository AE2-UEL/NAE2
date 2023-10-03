package co.neeve.nae2.common.interfaces;

import co.neeve.nae2.common.registries.Upgrades;

public interface IExtendedUpgradeInventory {
	int getInstalledUpgrades(Upgrades u);

	int getMaxInstalled(Upgrades u);
}
