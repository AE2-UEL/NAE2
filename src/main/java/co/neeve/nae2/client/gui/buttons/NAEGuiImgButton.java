package co.neeve.nae2.client.gui.buttons;

import appeng.client.gui.widgets.ITooltip;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.gui.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class NAEGuiImgButton extends GuiButton implements ITooltip {

	public static final ResourceLocation AESTATES = new ResourceLocation("appliedenergistics2",
		"textures/guis/states.png");
	private final ButtonType buttonType;
	private int buttonState;

	public NAEGuiImgButton(int x, int y, ButtonType buttonType) {
		super(0, 0, 16, "");
		this.buttonType = buttonType;
		this.x = x;
		this.y = y;
		this.width = 16;
		this.height = 16;
		this.enabled = true;
	}

	@Override
	public String getMessage() {
		var state = this.buttonType.getButtonStates()[this.getButtonState()];
		return I18n.format(state.getTranslationKey());
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
		return true;
	}

	@Override
	public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			var state = this.buttonType.getButtonStates()[this.getButtonState()];

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered =
				mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			mc.getTextureManager().bindTexture(AESTATES);
			this.drawTexturedModalRect(this.x, this.y, 240, 240, 16, 16);

			state.getSprite().draw(this.x, this.y);
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}

	public int getButtonState() {
		return this.buttonState;
	}

	public void setButtonState(int buttonState) {
		this.buttonState = MathHelper.clamp(buttonState, 0, this.buttonType.getButtonStates().length - 1);
	}

	public enum ButtonType {
		AUTO_PUSH(new ButtonState("auto_push"), new ButtonState("auto_push_off"));

		private final ButtonState[] buttonStates;

		ButtonType(ButtonState... buttonStates) {
			this.buttonStates = buttonStates;
		}

		private ButtonState[] getButtonStates() {
			return this.buttonStates;
		}
	}

	private static class ButtonState {
		private final String translationKey;
		private final Sprite sprite;

		public ButtonState(String resourceLocation) {
			this.translationKey = Tags.MODID + ".gui.button." + resourceLocation;

			this.sprite = new Sprite(new ResourceLocation(Tags.MODID,
				"textures/gui/button/" + resourceLocation + ".png"));
		}

		public String getTranslationKey() {
			return this.translationKey;
		}

		public Sprite getSprite() {
			return this.sprite;
		}
	}
}
