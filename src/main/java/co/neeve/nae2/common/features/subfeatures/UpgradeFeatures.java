package co.neeve.nae2.common.features.subfeatures;

import org.jetbrains.annotations.Nullable;

public enum UpgradeFeatures implements ISubFeature {
	HYPER_ACCELERATION("Enable Hyper-Acceleration Upgrade cards", "upgrades.hac");

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
