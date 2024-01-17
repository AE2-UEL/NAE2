package co.neeve.nae2.client.rendering.helpers;

import co.neeve.nae2.NAE2;
import co.neeve.nae2.client.rendering.helpers.beamformer.IBeamFormerRenderer;
import co.neeve.nae2.client.rendering.helpers.beamformer.renderers.DeprecatedBeamFormerRenderer;
import co.neeve.nae2.client.rendering.helpers.beamformer.renderers.ModernBeamFormerRenderer;
import co.neeve.nae2.client.rendering.helpers.beamformer.renderers.NativeBeamFormerRenderer;
import co.neeve.nae2.common.interfaces.IBeamFormer;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class BeamFormerRenderHelper {
	private static final double MIN_SCALE = 0.05d;
	private static final WeakHashMap<IBeamFormer, StaticBloomMetadata> META_CACHE = new WeakHashMap<>();
	private static final EnumSet<EnumFacing> FACINGS_ALONG_Z =
		EnumSet.of(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST);

	private static final EnumSet<EnumFacing> FACINGS_ALONG_X =
		EnumSet.of(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH);

	private static final EnumSet<EnumFacing> FACINGS_ALONG_Y =
		EnumSet.of(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST);

	private static final IBeamFormerRenderer RENDERER;

	static {
		IBeamFormerRenderer renderer;
		if ((renderer = ModernBeamFormerRenderer.create()) != null) {
			NAE2.logger()
				.info("Modern GregTech bloom API found. Beam formers will be rendered using that.");
		} else if ((renderer = DeprecatedBeamFormerRenderer.create()) != null) {
			NAE2.logger()
				.warn("Deprecated GregTech bloom API found. Beam formers will be rendered using that.");
		} else {
			renderer = NativeBeamFormerRenderer.create();
			NAE2.logger()
				.warn("No GregTech bloom API found. Beam formers will fall back to beacon rendering.");
		}

		RENDERER = renderer;
	}

	public static boolean shouldRenderDynamic(IBeamFormer partBeamFormer) {
		return RENDERER.shouldRenderDynamic(partBeamFormer);
	}


	public static void renderDynamic(IBeamFormer partBeamFormer, double x, double y, double z, float partialTicks) {
		RENDERER.renderDynamic(partBeamFormer, x, y, z, partialTicks);
	}

	public static float @NotNull [] getColor(IBeamFormer partBeamFormer) {
		var color = partBeamFormer.getColor();
		var scale = 255f;
		return new float[]{ ((color.mediumVariant >> 16) & 0xff) / scale,
			((color.mediumVariant >> 8) & 0xff) / scale,
			(color.mediumVariant & 0xff) / scale };
	}

	public static void drawCube(BufferBuilder bufferBuilder, double x, double y, double z, double length,
	                            StaticBloomMetadata result, float[] rgb) {
		EnumSet<EnumFacing> facings;
		final double scaleX;
		final double scaleY;
		final double scaleZ;

		// Determine which direction the beam is facing and draw accordingly
		if (result.dx != 0) {
			facings = FACINGS_ALONG_X;

			scaleX = Math.max(MIN_SCALE, Math.abs(result.dx * length + result.dx * 0.25d));
			scaleY = MIN_SCALE;
			scaleZ = MIN_SCALE;
		} else if (result.dy() != 0) {
			facings = FACINGS_ALONG_Y;

			scaleX = MIN_SCALE;
			scaleY = Math.max(MIN_SCALE, Math.abs(result.dy * length + result.dy * 0.25d));
			scaleZ = MIN_SCALE;
		} else if (result.dz() != 0) {
			facings = FACINGS_ALONG_Z;

			scaleX = MIN_SCALE;
			scaleY = MIN_SCALE;
			scaleZ = Math.max(MIN_SCALE, Math.abs(result.dz * length + result.dz * 0.25d));
		} else {
			return;
		}

		// Draw the beam cube using the provided metadata
		RenderUtils.drawCube(bufferBuilder, x, y, z,
			scaleX, scaleY, scaleZ,
			rgb, facings
		);
	}

	@NotNull
	public static BeamFormerRenderHelper.StaticBloomMetadata getBloomMetadata(IBeamFormer partBeamFormer) {
		final StaticBloomMetadata metadata;
		if ((metadata = META_CACHE.getOrDefault(partBeamFormer, null)) != null) {
			return metadata;
		} else {

			var facing = partBeamFormer.getDirection();
			final var dx = facing.getXOffset();
			final var dy = facing.getYOffset();
			final var dz = facing.getZOffset();
			final var pitch = (float) Math.atan2(Math.sqrt(dx * dx + dz * dz), dy) * (180F / (float) Math.PI);
			final var yaw = (float) (180 - Math.atan2(dz, dx) * (180F / (float) Math.PI) - 90.0F);

			var newMetadata = new StaticBloomMetadata(dx, dy, dz, pitch, yaw);
			META_CACHE.put(partBeamFormer, newMetadata);
			return newMetadata;
		}
	}

	public static void init(IBeamFormer partBeamFormer) {
		RENDERER.init(partBeamFormer);
	}

	@Desugar
	public record StaticBloomMetadata(int dx, int dy, int dz, float pitch, float yaw) {}
}
