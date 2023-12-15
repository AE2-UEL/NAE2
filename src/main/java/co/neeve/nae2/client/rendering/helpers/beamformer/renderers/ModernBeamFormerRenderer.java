package co.neeve.nae2.client.rendering.helpers.beamformer.renderers;

import co.neeve.nae2.client.rendering.helpers.beamformer.IBeamFormerRenderer;
import co.neeve.nae2.client.rendering.helpers.beamformer.setups.ModernBeamBloomSetup;
import co.neeve.nae2.common.interfaces.IBeamFormer;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.IBloomEffect;

import javax.annotation.Nullable;

import static co.neeve.nae2.client.rendering.helpers.BeamFormerRenderHelper.*;

public class ModernBeamFormerRenderer implements IBeamFormerRenderer {
	private final Object setup;

	private ModernBeamFormerRenderer() {
		this.setup = new ModernBeamBloomSetup();
	}

	public static @Nullable IBeamFormerRenderer create() {
		try {
			var clazz = Class.forName("gregtech.client.utils.IBloomEffect");
			if (clazz.isInterface()) {
				return new ModernBeamFormerRenderer();
			}
		} catch (Exception ignored) {}
		return null;
	}

	private static IBloomEffect getPredicate(final IBeamFormer partBeamFormer) {
		final var bp = partBeamFormer.getPos();
		final var x = bp.getX();
		final var y = bp.getY();
		final var z = bp.getZ();
		final var metadata = getBloomMetadata(partBeamFormer);

		return (bufferBuilder, ctx) -> {
			var rgb = getColor(partBeamFormer);
			final var beamLength = partBeamFormer.getBeamLength();
			final var beamLengthHalf = (beamLength + 1) / 2d;

			if (partBeamFormer.shouldRenderBeam()) {
				drawCube(bufferBuilder,
					0.5d + metadata.dx() * beamLengthHalf + x - ctx.cameraX(),
					0.5d + metadata.dy() * beamLengthHalf + y - ctx.cameraY(),
					0.5d + metadata.dz() * beamLengthHalf + z - ctx.cameraZ(),
					beamLength, metadata, rgb);
			}
		};
	}

	@Override
	public boolean shouldRenderDynamic(IBeamFormer partBeamFormer) {
		return false;
	}

	@Override
	public void init(IBeamFormer beamFormer) {
		final var predicate = getPredicate(beamFormer);
		BloomEffectUtil.registerBloomRender((IRenderSetup) this.setup,
			BloomType.UNITY,
			predicate,
			bloomRenderTicket -> beamFormer.isValid());
	}
}
