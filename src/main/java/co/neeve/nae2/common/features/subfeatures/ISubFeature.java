package co.neeve.nae2.common.features.subfeatures;

import javax.annotation.Nullable;

public interface ISubFeature {
	String name();

	@Nullable
	String getDescription();

	boolean isEnabled();

	void setEnabled(boolean enabled);

	@Nullable
	String getMixins();
}
