package co.neeve.nae2.client.gui;

import appeng.client.gui.AEBaseGui;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.gui.buttons.PatternMultiToolButton;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.items.patternmultitool.ObjPatternMultiTool;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

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

		ObjPatternMultiTool pmt = gui.getPMTObject();
		if (pmt != null) {
			int columns = pmt.getInstalledCapacityUpgrades();
			for (int i = 1; i <= columns; i++) {
				gui.drawTexturedModalRect(offsetX + gui.getPMTOffsetX() + 8 + (i * 18),
					offsetY + gui.getPMTOffsetY() + 8, 8, 8, 18, 18 * 9);
			}
		}
	}

	@SuppressWarnings({ "ArraysAsListWithZeroOrOneArgument" })
	public static List<Rectangle> getJEIExclusionArea(IPatternMultiToolHostGui gui) {
		return Arrays.asList(new Rectangle(gui.getGuiLeft() + gui.getPMTOffsetX(),
			gui.getGuiTop() + gui.getPMTOffsetY(), PMT_WIDTH, PMT_HEIGHT));
	}

	public static <T extends IPatternMultiToolHostGui> boolean hasClickedOutside(T gui, int mouseX, int mouseY,
	                                                                             int guiLeft, int guiTop) {
		int offsetX = gui.getGuiLeft() + gui.getPMTOffsetX();
		int offsetY = gui.getGuiTop() + gui.getPMTOffsetY();

		return !(mouseX >= offsetX && mouseX <= offsetX + PMT_WIDTH && mouseY >= offsetY && mouseY <= offsetY + PMT_HEIGHT);
	}

	public static void repositionButtons(AEBaseGui gui, List<PatternMultiToolButton> patternMultiToolButtons,
	                                     int offsetX, int offsetY) {
		for (PatternMultiToolButton button : patternMultiToolButtons) {
			button.y = button.getY() + offsetY;
			button.x = button.getX() + offsetX;
		}
	}
}
