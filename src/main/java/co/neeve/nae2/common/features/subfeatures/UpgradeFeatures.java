package co.neeve.nae2.common.features.subfeatures;

import appeng.util.Platform;
import org.jetbrains.annotations.Nullable;

public enum UpgradeFeatures implements ISubFeature {
	HYPER_ACCELERATION("Enable Hyper-Acceleration Upgrade cards for ME IO Ports", "upgrades.hac"),
	AUTO_COMPLETE("Enable Auto-Complete Upgrade cards for ME Interfaces", "upgrades.autocomplete"),
	GREGTECH_CIRCUIT("Enable GregTech Circuit Card for ME Interfaces", "upgrades.gregcircuit") {
		@Override
		public boolean isEnabled() {
			if (!super.isEnabled()) return false;

			if (Platform.isModLoaded("gregtech")) {
				try {
					return Class.forName("gregtech.api.capability.IGhostSlotConfigurable").isInterface();
				} catch (Throwable e) {
					// :P
				}
			}

			return false;
		}
	};

	private final String description;
	private final String mixins;
	private boolean enabled;

	UpgradeFeatures(String description, String mixins) {
		this.description = description;
		this.mixins = mixins;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Nullable
	@Override
	public String getMixins() {
		return this.mixins;
	}
}
