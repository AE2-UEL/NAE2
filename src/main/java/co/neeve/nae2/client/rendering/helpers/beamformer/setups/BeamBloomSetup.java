package co.neeve.nae2.client.rendering.helpers.beamformer.setups;

import gregtech.client.shader.postprocessing.BloomEffect;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

abstract class BeamBloomSetup {
	float lastBrightnessX;
	float lastBrightnessY;

	public void preDraw(@NotNull BufferBuilder buffer) {
		BloomEffect.strength = 0.8f;
		BloomEffect.baseBrightness = 0.0f;
		BloomEffect.highBrightnessThreshold = 1.25f;
		BloomEffect.lowBrightnessThreshold = 0.5f;
		BloomEffect.step = 1;

		this.lastBrightnessX = OpenGlHelper.lastBrightnessX;
		this.lastBrightnessY = OpenGlHelper.lastBrightnessY;

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		GlStateManager.disableTexture2D();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
	}

	public void postDraw(@NotNull BufferBuilder ignoredBuffer) {
		Tessellator.getInstance().draw();
		GlStateManager.enableTexture2D();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
			this.lastBrightnessX,
			this.lastBrightnessY);
	}
}
