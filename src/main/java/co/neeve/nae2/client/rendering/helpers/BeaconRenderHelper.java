package co.neeve.nae2.client.rendering.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BeaconRenderHelper {
	private static final ResourceLocation END_GATEWAY_BEAM_TEXTURE = new ResourceLocation("textures/entity" +
		"/end_gateway_beam.png");

	public static void renderBeamSegment(double x, double y, double z, double partialTicks, double textureScale,
	                                     double totalWorldTime, int yOffset, double height, float[] colors,
	                                     double beamRadius, double glowRadius) {
		Minecraft.getMinecraft().renderEngine.bindTexture(END_GATEWAY_BEAM_TEXTURE);

		var i = yOffset + height;
		GlStateManager.disableFog();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.glTexParameteri(3553, 10242, 10497);
		GlStateManager.glTexParameteri(3553, 10243, 10497);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.depthMask(true);

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
			GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		var tessellator = Tessellator.getInstance();
		var bufferbuilder = tessellator.getBuffer();
		var d0 = totalWorldTime + partialTicks;
		var d1 = height < 0 ? d0 : -d0;
		var d2 = MathHelper.frac(d1 * 0.2D - (double) MathHelper.floor(d1 * 0.1D));
		var f = colors[0];
		var f1 = colors[1];
		var f2 = colors[2];
		var d3 = d0 * 0.025D * -1.5D;
		var d4 = 0.5D + Math.cos(d3 + 2.356194490192345D) * beamRadius;
		var d5 = 0.5D + Math.sin(d3 + 2.356194490192345D) * beamRadius;
		var d6 = 0.5D + Math.cos(d3 + (Math.PI / 4D)) * beamRadius;
		var d7 = 0.5D + Math.sin(d3 + (Math.PI / 4D)) * beamRadius;
		var d8 = 0.5D + Math.cos(d3 + 3.9269908169872414D) * beamRadius;
		var d9 = 0.5D + Math.sin(d3 + 3.9269908169872414D) * beamRadius;
		var d10 = 0.5D + Math.cos(d3 + 5.497787143782138D) * beamRadius;
		var d11 = 0.5D + Math.sin(d3 + 5.497787143782138D) * beamRadius;
		var d13 = 1.0D;
		var d14 = -1.0D + d2;
		var d15 = height * textureScale * (0.5D / beamRadius) + d14;
		var alpha = 0.8f;
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ZERO);

		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(x + d4, y + i, z + d5).tex(1.0D, d15).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d4, y + (double) yOffset, z + d5).tex(1.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d6, y + (double) yOffset, z + d7).tex(0.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d6, y + i, z + d7).tex(0.0D, d15).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d10, y + i, z + d11).tex(1.0D, d15).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d10, y + (double) yOffset, z + d11).tex(1.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d8, y + (double) yOffset, z + d9).tex(0.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d8, y + i, z + d9).tex(0.0D, d15).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d6, y + i, z + d7).tex(1.0D, d15).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d6, y + (double) yOffset, z + d7).tex(1.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d10, y + (double) yOffset, z + d11).tex(0.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d10, y + i, z + d11).tex(0.0D, d15).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d8, y + i, z + d9).tex(1.0D, d15).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d8, y + (double) yOffset, z + d9).tex(1.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d4, y + (double) yOffset, z + d5).tex(0.0D, d14).color(f, f1, f2, alpha).endVertex();
		bufferbuilder.pos(x + d4, y + i, z + d5).tex(0.0D, d15).color(f, f1, f2, alpha).endVertex();
		tessellator.draw();

		GlStateManager.depthMask(false);
		d3 = 0.5D - glowRadius;
		d4 = 0.5D - glowRadius;
		d5 = 0.5D + glowRadius;
		d6 = 0.5D - glowRadius;
		d7 = 0.5D - glowRadius;
		d8 = 0.5D + glowRadius;
		d9 = 0.5D + glowRadius;
		d10 = 0.5D + glowRadius;
		d13 = -1.0D + d2;
		d14 = height * textureScale + d13;
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(x + d3, y + i, z + d4).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d3, y + (double) yOffset, z + d4).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d5, y + (double) yOffset, z + d6).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d5, y + i, z + d6).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d9, y + i, z + d10).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d9, y + (double) yOffset, z + d10).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d7, y + (double) yOffset, z + d8).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d7, y + i, z + d8).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d5, y + i, z + d6).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d5, y + (double) yOffset, z + d6).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d9, y + (double) yOffset, z + d10).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d9, y + i, z + d10).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d7, y + i, z + d8).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d7, y + (double) yOffset, z + d8).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d3, y + (double) yOffset, z + d4).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
		bufferbuilder.pos(x + d3, y + i, z + d4).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableFog();
	}
}
