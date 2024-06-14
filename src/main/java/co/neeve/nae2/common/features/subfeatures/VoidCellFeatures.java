package co.neeve.nae2.common.features.subfeatures;

import org.jetbrains.annotations.Nullable;

public enum VoidCellFeatures implements ISubFeature {
	CONDENSER_POWER("Enable Matter Condenser power", "void.condenser"),
	CONVERSION_RECIPES(
		"""
			Enable conversion recipes. Useful if you want to add custom recipes.
			HOWEVER, disassembling the Cells will still pop out a Void Component and a Housing!
			You might want to disable disassembling as a feature in the AE2 config, which Void Cells respect.""");

	private final String description;
	private final String mixins;
	private boolean enabled;

	VoidCellFeatures(String description, String mixins) {
		this.description = description;
		this.mixins = mixins;
	}

	VoidCellFeatures(String description) {
		this(description, null);
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
