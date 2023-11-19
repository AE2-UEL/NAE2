package co.neeve.nae2.common.features.subfeatures;

import co.neeve.nae2.common.features.IFeature;

import javax.annotation.Nullable;

public interface ISubFeature extends IFeature {
	String name();

	@Nullable
	String getDescription();

	void setEnabled(boolean enabled);

	@Nullable
	String getMixins();
}
