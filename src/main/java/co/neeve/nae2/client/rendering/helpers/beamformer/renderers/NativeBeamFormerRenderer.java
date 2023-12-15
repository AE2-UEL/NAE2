package co.neeve.nae2.client.rendering.helpers.beamformer.renderers;

import co.neeve.nae2.client.rendering.helpers.BeaconRenderHelper;
import co.neeve.nae2.client.rendering.helpers.beamformer.IBeamFormerRenderer;
import co.neeve.nae2.common.interfaces.IBeamFormer;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;

import static co.neeve.nae2.client.rendering.helpers.BeamFormerRenderHelper.getBloomMetadata;
import static co.neeve.nae2.client.rendering.helpers.BeamFormerRenderHelper.getColor;

public class NativeBeamFormerRenderer implements IBeamFormerRenderer {
	private NativeBeamFormerRenderer() {}

	public static @NotNull IBeamFormerRenderer create() {
		return new NativeBeamFormerRenderer();
	}

	@Override
	public boolean shouldRenderDynamic(IBeamFormer partBeamFormer) {
		return true;
	}

	@Override
	public void renderDynamic(IBeamFormer partBeamFormer, double x, double y, double z, float partialTicks) {
		var metadata = getBloomMetadata(partBeamFormer);
		var rgb = getColor(partBeamFormer);

		GlStateManager.pushMatrix();

		// Translate and rotate
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.rotate(metadata.yaw(), 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(metadata.pitch(), 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(-0.5, 0.35, -0.5);

		BeaconRenderHelper.renderBeamSegment(0, 0, 0, partialTicks, 1,
			(double) partBeamFormer.getWorld().getTotalWorldTime(), 0,
			partBeamFormer.getBeamLength() + 0.3d
			, rgb, 0.075 * 1.6, 0.075 * 2);

		GlStateManager.popMatrix();
	}
}
