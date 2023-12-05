package co.neeve.nae2.client.gui.buttons;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@SideOnly(CLIENT)
public class PMTSwitcherButton extends GuiButton implements ITooltip {
	private final RenderItem itemRenderer;
	private String message;
	private int hideEdge = 0;
	private ItemStack myItem;

	public PMTSwitcherButton(int x, int y, ItemStack ico, String message, RenderItem ir) {
		super(0, 0, 16, "");
		this.x = x;
		this.y = y;
		this.width = 22;
		this.height = 22;
		this.myItem = ico;
		this.message = message;
		this.itemRenderer = ir;
	}

	public void drawButton(@NotNull Minecraft minecraft, int x, int y, float partial) {
		if (this.visible) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			minecraft.renderEngine.bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states" +
				".png"));
			this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
			var uv_x = this.hideEdge > 0 ? 11 : 13;
			var offsetX = this.hideEdge > 0 ? 1 : 0;
			this.drawTexturedModalRect(this.x, this.y, uv_x * 16, 0, 25, 22);

			this.mouseDragged(minecraft, x, y);
			if (this.myItem != null) {
				this.zLevel = 100.0F;
				this.itemRenderer.zLevel = 100.0F;
				GlStateManager.enableDepth();
				RenderHelper.enableGUIStandardItemLighting();
				this.itemRenderer.renderItemAndEffectIntoGUI(this.myItem, offsetX + this.x + 3, this.y + 3);
				GlStateManager.disableDepth();
				this.itemRenderer.zLevel = 0.0F;
				this.zLevel = 0.0F;
			}
		}

	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int xPos() {
		return this.x;
	}

	public int yPos() {
		return this.y;
	}

	public int getWidth() {
		return 22;
	}

	public int getHeight() {
		return 22;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setHideEdge(int hideEdge) {
		this.hideEdge = hideEdge;
	}

	public void setItem(ItemStack item) {
		this.myItem = item;
	}
}

