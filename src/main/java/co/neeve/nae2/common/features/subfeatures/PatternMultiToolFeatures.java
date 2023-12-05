package co.neeve.nae2.common.features.subfeatures;

import org.jetbrains.annotations.Nullable;

public enum PatternMultiToolFeatures implements ISubFeature {
	MIXIN_INTO_AE_GUIS("Add Pattern Multi-Tool features to AE2 GUIs", "pmt");
	private final String description;
	private final String mixins;
	private boolean enabled;

	PatternMultiToolFeatures(String description, String mixins) {
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
