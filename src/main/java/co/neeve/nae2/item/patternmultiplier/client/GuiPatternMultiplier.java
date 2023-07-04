package co.neeve.nae2.item.patternmultiplier.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.Tags;
import co.neeve.nae2.item.patternmultiplier.ObjPatternMultiplier;
import co.neeve.nae2.item.patternmultiplier.container.ContainerPatternMultiplier;
import co.neeve.nae2.item.patternmultiplier.net.PatternMultiplierPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class GuiPatternMultiplier extends AEBaseGui {
    // GUI texture
    private final ResourceLocation loc = new ResourceLocation(Tags.MODID, "textures/gui/pattern_multiplier.png");

    // Constructor
    public GuiPatternMultiplier(InventoryPlayer inventoryPlayer, ObjPatternMultiplier te) {
        super(new ContainerPatternMultiplier(inventoryPlayer, te));
        this.ySize = 189;
    }

    // Handles button actions
    @Override
    protected void actionPerformed(@NotNull GuiButton btn) throws IOException {
        super.actionPerformed(btn);
        // Check which button was pressed and send corresponding packet
        NAE2.network.sendToServer(new PatternMultiplierPacket(btn.id));
    }

    // Initializes the GUI
    @Override
    public void initGui() {
        super.initGui();

        // Calculate start position for buttons
        int totalButtonW = 20 * 3 + 60 + 3 * 3;
        int start = 1 + this.guiLeft + this.xSize / 2 - totalButtonW / 2;

        // Add buttons to the GUI
        // GUI buttons
        this.buttonList.add(new TooltipButton(0, start, this.guiTop + 76, "*2",
                ButtonToolTips.MultiplyByTwo, ButtonToolTips.MultiplyByTwoDesc));
        this.buttonList.add(new TooltipButton(1, start + 23, this.guiTop + 76, "*3",
                ButtonToolTips.MultiplyByThree, ButtonToolTips.MultiplyByThreeDesc));
        this.buttonList.add(new TooltipButton(2, start + 46, this.guiTop + 76, "+1",
                ButtonToolTips.IncreaseByOne, ButtonToolTips.IncreaseByOneDesc));

        this.buttonList.add(new TooltipButton(3, start, this.guiTop + 76, "/2",
                ButtonToolTips.DivideByTwo, ButtonToolTips.DivideByTwoDesc));
        this.buttonList.add(new TooltipButton(4, start + 23, this.guiTop + 76, "/3",
                ButtonToolTips.DivideByThree, ButtonToolTips.DivideByThreeDesc));
        this.buttonList.add(new TooltipButton(5, start + 46, this.guiTop + 76, "-1",
                ButtonToolTips.DecreaseByOne, ButtonToolTips.DecreaseByOneDesc));

        GuiButton unencode;
        this.buttonList.add(unencode = new TooltipButton(6, start + 69, this.guiTop + 76, "Unencode",
                "nae2.pattern_multiplier.unencode", "nae2.pattern_multiplier.unencode.desc"));
        unencode.width = 60;

    }

    // Draws the screen
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        boolean shiftDown = isShiftKeyDown();
        this.buttonList.subList(0, 3).forEach(but -> but.visible = !shiftDown);
        this.buttonList.subList(3, 6).forEach(but -> but.visible = shiftDown);
    }

    // Draws the foreground
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("item.nae2.pattern_multiplier.name"), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    // Draws the background
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(loc);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }

    public static class TooltipButton extends GuiButton implements ITooltip {
        private final String title;
        private final String hint;


        public TooltipButton(int buttonId, int x, int y, String buttonText, ButtonToolTips title, ButtonToolTips hint) {
            this(buttonId, x, y, buttonText, title.getUnlocalized(), hint.getUnlocalized());
        }
        public TooltipButton(int buttonId, int x, int y, String buttonText, String title, String hint) {
            super(buttonId, x, y, buttonText);
            this.width = 18;
            this.height = 18;
            this.title = title;
            this.hint = hint;
        }

        @Override
        public String getMessage() {
            return I18n.format(this.title) + "\n" + I18n.format(this.hint);
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
}

