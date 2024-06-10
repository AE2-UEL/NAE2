package co.neeve.nae2.common.integration.ae2fc;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;

public class AE2FC {

	public static void initInterfaceUpgrade(Upgrades.UpgradeType upgrade) {
		upgrade.registerItem(new ItemStack(FCBlocks.DUAL_INTERFACE), 1);
		upgrade.registerItem(new ItemStack(FCItems.PART_DUAL_INTERFACE), 1);
	}

	public static void postInit(Side side) {
		NAE2.definitions().parts().p2pTunnelInterface().maybeStack(1).ifPresent((p2pTunnel) -> {
			try {
				var tunnelType = Enum.valueOf(TunnelType.class, "NAE2_IFACE_P2P");
				var reg = AEApi.instance().registries().p2pTunnel();
				reg.addNewAttunement(new ItemStack(FCBlocks.DUAL_INTERFACE), tunnelType);
				reg.addNewAttunement(new ItemStack(FCItems.PART_DUAL_INTERFACE), tunnelType);
			} catch (IllegalArgumentException ignored) {}
		});
	}
}
