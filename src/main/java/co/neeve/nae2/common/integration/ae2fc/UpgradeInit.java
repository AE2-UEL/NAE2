package co.neeve.nae2.common.integration.ae2fc;

import co.neeve.nae2.common.registration.definitions.Upgrades;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import net.minecraft.item.ItemStack;

public class UpgradeInit {

    public static void init(Upgrades.UpgradeType upgrade) {
        upgrade.registerItem(new ItemStack(FCBlocks.DUAL_INTERFACE), 1);
        upgrade.registerItem(new ItemStack(FCItems.PART_DUAL_INTERFACE), 1);
    }

}
