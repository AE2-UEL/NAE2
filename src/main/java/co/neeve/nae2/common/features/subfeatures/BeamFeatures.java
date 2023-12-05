package co.neeve.nae2.common.features.subfeatures;

import org.jetbrains.annotations.Nullable;

public enum BeamFeatures implements ISubFeature {
	GREGTECH_SHADERS("Use GregTech CEu shaders if possible");

	private final String description;
	private boolean enabled;

	BeamFeatures(String description) {
		this.description = description;
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
		return null;
	}
}
