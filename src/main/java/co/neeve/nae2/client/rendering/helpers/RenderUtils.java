package co.neeve.nae2.client.rendering.helpers;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;

@SideOnly(Side.CLIENT)
public class RenderUtils {
	/**
	 * Draws only the specified faces in facings.
	 */
	public static void drawCube(BufferBuilder bufferBuilder, double x, double y, double z, double scaleX,
	                            double scaleY, double scaleZ, float[] rgb, EnumSet<EnumFacing> facings) {
		if (facings.isEmpty()) return;

		// Calculate the offset for each axis
		var halfScaleX = scaleX / 2;
		var halfScaleY = scaleY / 2;
		var halfScaleZ = scaleZ / 2;

		// Bottom vertices of the cube
		var x1 = x - halfScaleX;
		var y1 = y - halfScaleY;
		var z1 = z - halfScaleZ;

		// Top vertices of the cube
		var x2 = x + halfScaleX;
		var y2 = y + halfScaleY;
		var z2 = z + halfScaleZ;

		var r = rgb[0];
		var g = rgb[1];
		var b = rgb[2];

		// Down face
		var alpha = 0.9f;
		if (facings.contains(EnumFacing.DOWN)) {
			bufferBuilder.pos(x1, y1, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y1, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y1, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x1, y1, z2).color(r, g, b, alpha).endVertex();
		}

		// Up face
		if (facings.contains(EnumFacing.UP)) {
			bufferBuilder.pos(x1, y2, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x1, y2, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y2, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y2, z1).color(r, g, b, alpha).endVertex();
		}

		// North face
		if (facings.contains(EnumFacing.NORTH)) {
			bufferBuilder.pos(x1, y1, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x1, y2, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y2, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y1, z1).color(r, g, b, alpha).endVertex();
		}

		// South face
		if (facings.contains(EnumFacing.SOUTH)) {
			bufferBuilder.pos(x1, y1, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y1, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y2, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x1, y2, z2).color(r, g, b, alpha).endVertex();
		}

		// West face
		if (facings.contains(EnumFacing.WEST)) {
			bufferBuilder.pos(x1, y1, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x1, y1, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x1, y2, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x1, y2, z1).color(r, g, b, alpha).endVertex();
		}

		// East face
		if (facings.contains(EnumFacing.EAST)) {
			bufferBuilder.pos(x2, y1, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y2, z1).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y2, z2).color(r, g, b, alpha).endVertex();
			bufferBuilder.pos(x2, y1, z2).color(r, g, b, alpha).endVertex();
		}
	}
}
