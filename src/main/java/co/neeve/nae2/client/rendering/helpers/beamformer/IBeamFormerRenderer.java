package co.neeve.nae2.client.rendering.helpers.beamformer;

import co.neeve.nae2.common.interfaces.IBeamFormer;

public interface IBeamFormerRenderer {
	default void init(IBeamFormer beamFormer) {}

	boolean shouldRenderDynamic(IBeamFormer partBeamFormer);

	default void renderDynamic(IBeamFormer partBeamFormer, double x, double y, double z, float partialTicks) {}
}
