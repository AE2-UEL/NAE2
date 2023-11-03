package co.neeve.nae2.mixin.upgrades.base.machine;

import appeng.parts.automation.StackUpgradeInventory;
import co.neeve.nae2.common.registries.Upgrades;
import co.neeve.nae2.mixin.upgrades.base.MixinUpgradeInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = StackUpgradeInventory.class, remap = false)
public class MixinStackUpgradeInventory extends MixinUpgradeInventory {
	@Shadow
	@Final
	private ItemStack stack;

	@Unique
	@Override
	public int getMaxInstalled(Upgrades upgrades) {
		int max = 0;

		for (ItemStack is : upgrades.getSupported().keySet()) {
			if (ItemStack.areItemsEqual(this.stack, is)) {
				max = upgrades.getSupported().get(is);
				break;
			}
		}

		return max;
	}
}
