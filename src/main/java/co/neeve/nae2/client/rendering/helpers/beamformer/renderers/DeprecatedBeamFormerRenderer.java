package co.neeve.nae2.client.rendering.helpers.beamformer.renderers;

import co.neeve.nae2.client.rendering.helpers.beamformer.IBeamFormerRenderer;
import co.neeve.nae2.client.rendering.helpers.beamformer.setups.DeprecatedBeamBloomSetup;
import co.neeve.nae2.common.interfaces.IBeamFormer;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.client.renderer.BufferBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static co.neeve.nae2.client.rendering.helpers.BeamFormerRenderHelper.*;

public class DeprecatedBeamFormerRenderer implements IBeamFormerRenderer {
	private final Object setup;

	private DeprecatedBeamFormerRenderer() {
		this.setup = new DeprecatedBeamBloomSetup();
	}

	public static @Nullable IBeamFormerRenderer create() {
		try {
			var clazz = Class.forName("gregtech.client.utils.BloomEffectUtil$IBloomRenderFast");
			if (clazz.isInterface()) {
				return new DeprecatedBeamFormerRenderer();
			}
		} catch (Exception ignored) {}
		return null;
	}

	private static Consumer<BufferBuilder> getPredicate(final IBeamFormer partBeamFormer,
	                                                    double x, double y, double z) {
		return bufferBuilder -> {
			var meta = getBloomMetadata(partBeamFormer);
			var color = getColor(partBeamFormer);

			final var beamLength = partBeamFormer.getBeamLength();
			final var beamLengthHalf = (beamLength + 1) / 2d;

			drawCube(bufferBuilder,
				x + 0.5d + meta.dx() * beamLengthHalf,
				y + 0.5d + meta.dy() * beamLengthHalf,
				z + 0.5d + meta.dz() * beamLengthHalf,
				beamLength, meta, color);
		};
	}


	@Override
	public boolean shouldRenderDynamic(IBeamFormer partBeamFormer) {
		return true;
	}

	@Override
	@SuppressWarnings({ "UnstableApiUsage", "deprecation" })
	public void renderDynamic(IBeamFormer partBeamFormer, double x, double y, double z, float partialTicks) {
		BloomEffectUtil.requestCustomBloom((BloomEffectUtil.IBloomRenderFast) this.setup,
			getPredicate(partBeamFormer, x, y, z));
	}
}
