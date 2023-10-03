package co.neeve.nae2.common.interfaces;

import co.neeve.nae2.common.registries.Upgrades;
import net.minecraft.item.ItemStack;

public interface INAEUpgradeModule {
	Upgrades getType(ItemStack is);
}
