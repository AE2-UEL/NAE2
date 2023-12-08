package co.neeve.nae2.common.features;

import appeng.util.Platform;
import co.neeve.nae2.common.features.subfeatures.*;

import javax.annotation.Nullable;
import java.util.EnumSet;

// TODO: this should optimally be split into factories.
public enum Features implements IFeature {
	PATTERN_MULTI_TOOL(EnumSet.allOf(PatternMultiToolFeatures.class)),
	VOID_CELLS(EnumSet.allOf(VoidCellFeatures.class), "void"),
	BEAM_FORMERS(EnumSet.allOf(BeamFeatures.class), "beam_former"),
	JEI_HOOKS(EnumSet.allOf(JEIFeatures.class)) {
		@Override
		public boolean isEnabled() {
			return Platform.isModLoaded("jei") && super.isEnabled();
		}
	},
	INTERFACE_P2P() {
		@Override
		public String[] getMixins() {
			return Platform.isModLoaded("ae2fc")
				? new String[]{ "ifacep2p", "ifacep2p.ae2fc" }
				: new String[]{ "ifacep2p" };
		}
	},
	UPGRADES(EnumSet.allOf(UpgradeFeatures.class), "upgrades"),
	RECONSTRUCTION_CHAMBER("reconchamber") {
		@Override
		public boolean isEnabled() {
			return Platform.isModLoaded("actuallyadditions") && super.isEnabled();
		}
	},
	DENSE_CELLS(EnumSet.allOf(DenseCellFeatures.class)),
	DENSE_CPU_COPROCESSORS("dense.coprocessor"),
	DENSE_FLUID_CELLS();

	private String[] mixins;
	private EnumSet<? extends ISubFeature> subFeatures = null;
	private boolean enabled;

	Features() {}

	Features(String mixins) {
		this();
		this.mixins = new String[]{ mixins };
	}

	Features(EnumSet<? extends ISubFeature> subFeatures) {
		this.subFeatures = subFeatures;
	}

	Features(EnumSet<? extends ISubFeature> subFeatures, String mixins) {
		this(subFeatures);

		this.mixins = new String[]{ mixins };
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Nullable
	public EnumSet<? extends ISubFeature> getSubFeatures() {
		return this.subFeatures;
	}

	@Nullable
	public String[] getMixins() {
		return this.mixins;
	}
}
