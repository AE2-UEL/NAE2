package co.neeve.nae2.common.features.subfeatures;

import org.jetbrains.annotations.Nullable;

public enum JEIFeatures implements ISubFeature {
	CTRL_CRAFT("Enable holding Control to mass-order missing items", "jei.craft"),
	CELL_VIEW("Enable viewing Storage Cell contents using JEI recipes", "jei.cellview");

	private final String description;
	private final String mixins;
	private boolean enabled;

	JEIFeatures(String description, String mixins) {
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
