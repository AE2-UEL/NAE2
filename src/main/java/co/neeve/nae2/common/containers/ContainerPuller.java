package co.neeve.nae2.common.containers;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.SlotOversized;
import co.neeve.nae2.common.helpers.Puller;
import co.neeve.nae2.common.parts.implementations.IPullerHost;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPuller extends ContainerUpgradeable {

	private final IPullerHost pullerHost;

	@GuiSync(3)
	public int autoPushMode = 0;

	public ContainerPuller(InventoryPlayer ip, IPullerHost pullerHost) {
		super(ip, pullerHost);
		this.pullerHost = pullerHost;
		var inv = pullerHost.getPuller().getInternalInventory();

		for (var i = 0; i < 3; ++i) {
			for (var j = 0; j < 3; ++j) {
				this.addSlotToContainer(new SlotOversized(inv, j + i * 3, 62 + j * 18, 17 + i * 18));
			}
		}
	}

	@Override
	protected boolean supportCapacity() {
		return false;
	}

	@Override
	public int availableUpgrades() {
		return 0;
	}

	@Override
	protected void setupConfig() {}

	@Override
	public void loadSettingsFromHost(IConfigManager cm) {
		this.setAutoPushMode((YesNo) cm.getSetting(Settings.PLACE_BLOCK));
	}

	private Puller getPuller() {
		return this.pullerHost.getPuller();
	}

	@Override
	public int getHeight() {
		return 166;
	}

	public int getAutoPushMode() {
		return this.autoPushMode;
	}

	public void setAutoPushMode(YesNo autoPushMode) {
		this.autoPushMode = autoPushMode == YesNo.YES ? 0 : 1;
	}
}
