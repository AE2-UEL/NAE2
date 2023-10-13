package co.neeve.nae2.common.features;

import co.neeve.nae2.common.features.subfeatures.*;

import javax.annotation.Nullable;
import java.util.EnumSet;

// TODO: this should optimally be split into factories.
public enum Features {
	PATTERN_MULTI_TOOL(EnumSet.allOf(PatternMultiToolFeatures.class)),
	VOID_CELLS(EnumSet.allOf(VoidCellFeatures.class)),
	BEAM_FORMERS("beam_former"),
	JEI_HOOKS(EnumSet.allOf(JEIFeatures.class)),
	INTERFACE_P2P("ifacep2p"),
	UPGRADES(EnumSet.allOf(UpgradeFeatures.class), "upgrades");

	private String mixins;
	private EnumSet<? extends ISubFeature> subFeatures = null;
	private boolean enabled;

	Features() {}

	Features(String mixins) {
		this();
		this.mixins = mixins;
	}

	Features(EnumSet<? extends ISubFeature> subFeatures) {
		this.subFeatures = subFeatures;
	}

	Features(EnumSet<? extends ISubFeature> subFeatures, String mixins) {
		this(subFeatures);

		this.mixins = mixins;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Nullable
	public EnumSet<? extends ISubFeature> getSubFeatures() {
		return subFeatures;
	}

	@Nullable
	public String getMixins() {
		return mixins;
	}
}
