package co.neeve.nae2.client.gui.buttons;

import appeng.client.gui.widgets.ITooltip;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.gui.implementations.GuiPatternMultiTool;
import co.neeve.nae2.common.containers.ContainerPatternMultiTool;
import co.neeve.nae2.common.enums.PatternMultiToolTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@SideOnly(CLIENT)
public class PMTTabButton extends GuiButton implements ITooltip {
	public static final ResourceLocation RESOURCE_LOCATION =
		new ResourceLocation(Tags.MODID, "textures/gui/pattern_multiplier.png");
	private final GuiPatternMultiTool host;
	private final PatternMultiToolTabs tab;
	private final String message;

	public PMTTabButton(GuiPatternMultiTool host, PatternMultiToolTabs tab, int x, int y) {
		super(0, 0, 16, "");
		this.host = host;
		this.tab = tab;
		this.y = y;

		var sw = Minecraft.getMinecraft().fontRenderer.getStringWidth(tab.getLocal());
		this.width = 5 + sw + 5;
		this.x = x - this.width;
		this.height = 17;
		this.message = tab.getLocal();
	}

	private static void drawStretchedTexture(int x, int y, int u, int v, int width) {
		var texHeight = 256f;
		var texWidth = 256f;
		var uvU = u / texWidth;
		var uvV = v / texHeight;
		var rectHeight = 18.0F;

		// Draw the rectangle.
		var tessellator = Tessellator.getInstance();
		var buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + rectHeight, 0.0D).tex(uvU, uvV + rectHeight / texHeight).endVertex();
		buffer.pos(x + (float) width, y + rectHeight, 0.0D).tex(uvU, uvV + rectHeight / texHeight).endVertex();
		buffer.pos(x + (float) width, y, 0.0D).tex(uvU, uvV).endVertex();
		buffer.pos(x, y, 0.0D).tex(uvU, uvV).endVertex();
		tessellator.draw();
	}

	public void drawButton(@NotNull Minecraft minecraft, int x, int y, float partial) {
		if (this.visible) {
			var color = 1F;
			if (((ContainerPatternMultiTool) this.host.inventorySlots).getViewingTab() != this.tab) {
				color = 0.5F;
			}
			GlStateManager.color(color, color, color, 1.0F);
			minecraft.renderEngine.bindTexture(RESOURCE_LOCATION);
			this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
			var uv_x = 179;
			var uv_y = 35;
			this.drawTexturedModalRect(this.x, this.y, uv_x, uv_y, 4, 17);

			drawStretchedTexture(this.x + 4, this.y - 1, 183, 34, this.width - 4);

			minecraft.fontRenderer.drawStringWithShadow(this.message, this.x + 5, this.y + 4, 16777215);

			this.mouseDragged(minecraft, x, y);
		}

	}

	public String getMessage() {
		return this.message;
	}

	public int xPos() {
		return this.x;
	}

	public int yPos() {
		return this.y;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public PatternMultiToolTabs getTab() {
		return this.tab;
	}
}

