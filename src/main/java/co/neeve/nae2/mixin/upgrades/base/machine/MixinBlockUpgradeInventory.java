package co.neeve.nae2.mixin.upgrades.base.machine;

import appeng.parts.automation.BlockUpgradeInventory;
import co.neeve.nae2.common.registries.Upgrades;
import co.neeve.nae2.mixin.upgrades.base.MixinUpgradeInventory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockUpgradeInventory.class)
public abstract class MixinBlockUpgradeInventory extends MixinUpgradeInventory {
	@Shadow
	@Final
	private Block block;

	@Unique
	@Override
	public int getMaxInstalled(Upgrades upgrades) {
		int max = 0;

		for (ItemStack is : upgrades.getSupported().keySet()) {
			Item encodedItem = is.getItem();
			if (encodedItem instanceof ItemBlock && Block.getBlockFromItem(encodedItem) == this.block) {
				max = upgrades.getSupported().get(is);
				break;
			}
		}

		return max;
	}
}
