package co.neeve.nae2.common.features.subfeatures;

import org.jetbrains.annotations.Nullable;

public enum VoidCellFeatures implements ISubFeature {
	CONDENSER_POWER("Enable Matter Condenser power", "void.condenser");

	private final String description;
	private final String mixins;
	private boolean enabled;

	VoidCellFeatures(String description, String mixins) {
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
