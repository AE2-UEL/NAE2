package co.neeve.nae2.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class Sprite {
	private static final int HEIGHT = 16;
	private static final int WIDTH = 16;

	private final ResourceLocation resourceLocation;

	public Sprite(ResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	public void draw(int x, int y) {
		Minecraft.getMinecraft().renderEngine.bindTexture(this.resourceLocation);
		var tessellator = Tessellator.getInstance();
		var bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, y + HEIGHT, 0.0D).tex(0, 1).endVertex();
		bufferbuilder.pos(x + WIDTH, y + HEIGHT, 0.0D).tex(1, 1).endVertex();
		bufferbuilder.pos(x + WIDTH, y, 0.0D).tex(1, 0).endVertex();
		bufferbuilder.pos(x, y, 0.0D).tex(0, 0).endVertex();
		tessellator.draw();
	}
}
