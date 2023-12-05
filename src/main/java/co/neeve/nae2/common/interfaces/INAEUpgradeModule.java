package co.neeve.nae2.common.interfaces;

import co.neeve.nae2.common.registration.definitions.Upgrades;
import net.minecraft.item.ItemStack;

public interface INAEUpgradeModule {
	Upgrades.UpgradeType getType(ItemStack is);
}
