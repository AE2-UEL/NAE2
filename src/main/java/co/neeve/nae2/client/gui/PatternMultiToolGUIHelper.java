package co.neeve.nae2.client.gui;

import co.neeve.nae2.Tags;
import co.neeve.nae2.client.gui.buttons.PatternMultiToolButton;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@SideOnly(Side.CLIENT)
public class PatternMultiToolGUIHelper {
	public static final int PMT_WIDTH = 86;
	public static final int PMT_HEIGHT = 198;
	private static final ResourceLocation loc = new ResourceLocation(Tags.MODID, "textures/gui" +
		"/pattern_multiplier_toolbox.png");

	public static <T extends GuiScreen & IPatternMultiToolHostGui> void drawPMTGui(T gui, int offsetX, int offsetY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		gui.mc.getTextureManager().bindTexture(loc);
		gui.drawTexturedModalRect(offsetX + gui.getPMTOffsetX(), offsetY + gui.getPMTOffsetY(), 0, 0, PMT_WIDTH,
			PMT_HEIGHT);

		var pmt = gui.getPMTObject();
		if (pmt != null) {
			var columns = pmt.getInstalledCapacityUpgrades();
			for (var i = 1; i <= columns; i++) {
				gui.drawTexturedModalRect(offsetX + gui.getPMTOffsetX() + 8 + (i * 18),
					offsetY + gui.getPMTOffsetY() + 8, 8, 8, 16, 18 * 9);
			}
		}
	}

	@SuppressWarnings({ "ArraysAsListWithZeroOrOneArgument" })
	public static List<Rectangle> getJEIExclusionArea(IPatternMultiToolHostGui gui) {
		return Arrays.asList(new Rectangle(gui.getGuiLeft() + gui.getPMTOffsetX(),
			gui.getGuiTop() + gui.getPMTOffsetY(), PMT_WIDTH, PMT_HEIGHT));
	}

	public static <T extends IPatternMultiToolHostGui> boolean hasClickedOutside(T gui, int mouseX, int mouseY) {
		var offsetX = gui.getGuiLeft() + gui.getPMTOffsetX();
		var offsetY = gui.getGuiTop() + gui.getPMTOffsetY();

		return !(mouseX >= offsetX && mouseX <= offsetX + PMT_WIDTH && mouseY >= offsetY && mouseY <= offsetY + PMT_HEIGHT);
	}

	public static void repositionButtons(List<PatternMultiToolButton> patternMultiToolButtons,
	                                     int offsetX, int offsetY) {
		for (var button : patternMultiToolButtons) {
			button.y = button.getY() + offsetY;
			button.x = button.getX() + offsetX;
		}
	}
}
