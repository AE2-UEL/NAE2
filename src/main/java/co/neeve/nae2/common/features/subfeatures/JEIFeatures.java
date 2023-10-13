package co.neeve.nae2.common.features.subfeatures;

import org.jetbrains.annotations.Nullable;

public enum JEIFeatures implements ISubFeature {
	CTRL_CRAFT("Enable holding Control to mass-order missing items", "jei.craft");

	private final String description;
	private final String mixins;
	private boolean enabled;

	JEIFeatures(String description, String mixins) {
		this.description = description;
		this.mixins = mixins;
	}

	public String getDescription() {
		return description;
	}

	public boolean isEnabled() {
		return enabled;
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
