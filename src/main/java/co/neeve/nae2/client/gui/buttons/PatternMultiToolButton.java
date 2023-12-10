package co.neeve.nae2.client.gui.buttons;

import appeng.client.gui.widgets.ITooltip;
import co.neeve.nae2.common.enums.PatternMultiToolActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class PatternMultiToolButton extends GuiButton implements ITooltip {
	private final PatternMultiToolActions action;
	private final int defX;
	private final int defY;
	private String overrideName;

	public PatternMultiToolButton(int x, int y, PatternMultiToolActions action) {
		super(0, x, y, "");
		this.action = action;
		this.width = 18;
		this.height = 20;

		this.defX = x;
		this.defY = y;
	}

	public int getX() {
		return this.defX;
	}

	public int getY() {
		return this.defY;
	}

	private boolean isShiftHeld() {
		return GuiScreen.isShiftKeyDown();
	}

	public void setOverrideName(String val) {
		this.overrideName = val;
	}

	public PatternMultiToolActions getAction() {
		var shiftAction = this.action.getShiftAction();
		return this.isShiftHeld() && shiftAction != null ? shiftAction : this.action;
	}

	@Override
	public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			var fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered =
				mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			var i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20,
				this.width / 2, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
			var j = 14737632;

			if (this.packedFGColour != 0) {
				j = this.packedFGColour;
			} else if (!this.enabled) {
				j = 10526880;
			} else if (this.hovered) {
				j = 16777120;
			}

			this.drawCenteredString(fontrenderer, I18n.format(this.overrideName != null ? this.overrideName :
					this.getAction().getName()), this.x + this.width / 2,
				this.y + (this.height - 8) / 2, j);
		}
	}

	@Override
	public String getMessage() {
		var action = this.getAction();
		return I18n.format(action.getTitle()) + "\n" + I18n.format(action.getDesc());
	}

	@Override
	public int xPos() {
		return this.x;
	}

	@Override
	public int yPos() {
		return this.y;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}
}

