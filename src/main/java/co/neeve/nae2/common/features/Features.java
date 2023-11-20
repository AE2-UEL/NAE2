package co.neeve.nae2.common.features;

import appeng.util.Platform;
import co.neeve.nae2.common.features.subfeatures.*;

import javax.annotation.Nullable;
import java.util.EnumSet;

// TODO: this should optimally be split into factories.
public enum Features implements IFeature {
	PATTERN_MULTI_TOOL(EnumSet.allOf(PatternMultiToolFeatures.class)),
	VOID_CELLS(EnumSet.allOf(VoidCellFeatures.class)),
	BEAM_FORMERS(EnumSet.allOf(BeamFeatures.class), "beam_former"),
	JEI_HOOKS(EnumSet.allOf(JEIFeatures.class)) {
		@Override
		public boolean isEnabled() {
			return Platform.isModLoaded("jei") && super.isEnabled();
		}
	},
	INTERFACE_P2P("ifacep2p"),
	UPGRADES(EnumSet.allOf(UpgradeFeatures.class), "upgrades"),
	RECONSTRUCTION_CHAMBER("reconchamber") {
		@Override
		public boolean isEnabled() {
			return Platform.isModLoaded("actuallyadditions") && super.isEnabled();
		}
	};

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
