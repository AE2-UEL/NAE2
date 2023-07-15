package co.neeve.nae2.client.gui.implementations;

import appeng.api.AEApi;
import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import appeng.helpers.IInterfaceHost;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.gui.buttons.NAE2GuiTabButton;
import co.neeve.nae2.client.gui.buttons.PatternMultiToolButton;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.common.containers.ContainerPatternMultiTool;
import co.neeve.nae2.common.enums.PatternMultiToolActions;
import co.neeve.nae2.common.enums.PatternMultiToolInventories;
import co.neeve.nae2.items.patternmultitool.ObjPatternMultiTool;
import co.neeve.nae2.items.patternmultitool.ToolPatternMultiTool;
import co.neeve.nae2.items.patternmultitool.net.PatternMultiToolPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class GuiPatternMultiTool extends AEBaseGui implements IPatternMultiToolHostGui {
	private static final ItemStack INTERFACE_ICON =
		AEApi.instance().definitions().blocks().iface().maybeStack(1).orElse(ItemStack.EMPTY);
	private static final ItemStack PMT_ICON = new ItemStack(new ToolPatternMultiTool(), 1);
	// GUI texture
	private static final ResourceLocation loc = new ResourceLocation(Tags.MODID, "textures/gui/pattern_multiplier" +
		".png");
	private final ContainerPatternMultiTool containerPatternMultiTool;
	private NAE2GuiTabButton switcherButton;

	// Constructor
	public GuiPatternMultiTool(InventoryPlayer inventoryPlayer, ObjPatternMultiTool te, IInterfaceHost iface) {
		super(new ContainerPatternMultiTool(inventoryPlayer, te, iface));
		this.containerPatternMultiTool = (ContainerPatternMultiTool) this.inventorySlots;
		this.ySize = 189 + 18;
		this.xSize = 211;
	}

	// Handles button actions
	@Override
	protected void actionPerformed(@NotNull GuiButton btn) throws IOException {
		super.actionPerformed(btn);

		if (btn == this.switcherButton) {
			NAE2.network.sendToServer(new PatternMultiToolPacket(PatternMultiToolActions.INV_SWITCH.ordinal()));
		}
	}

	// Initializes the GUI
	@Override
	public void initGui() {
		super.initGui();

		// Calculate start position for buttons
		int start = 7 + this.guiLeft;

		// Add buttons to the GUI
		this.buttonList.add(new PatternMultiToolButton(start, this.guiTop + 76 + 18, PatternMultiToolActions.MUL2));
		this.buttonList.add(new PatternMultiToolButton(start + 23, this.guiTop + 76 + 18,
			PatternMultiToolActions.MUL3));
		this.buttonList.add(new PatternMultiToolButton(start + 46, this.guiTop + 76 + 18,
			PatternMultiToolActions.ADD));

		GuiButton unencode;
		this.buttonList.add(unencode = new PatternMultiToolButton(start + 176 - 60 - 15, this.guiTop + 76 + 18,
			PatternMultiToolActions.CLEAR));
		unencode.width = 60;

		// Draw Interface/PMT switcher
		if (containerPatternMultiTool.isBoundToInterface()) {
			switcherButton = new NAE2GuiTabButton(this.guiLeft + 152 - 1, this.guiTop + 3 - 18 + 11, PMT_ICON,
				GuiText.Priority.getLocal(), this.itemRender);
			switcherButton.setHideEdge(1);
			switcherButton.id = 7;
			this.buttonList.add(switcherButton);
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if (switcherButton != null) {
			boolean b = this.getContainer().viewingInventory == PatternMultiToolInventories.INTERFACE;
			switcherButton.setItem(b ? INTERFACE_ICON : PMT_ICON);
			switcherButton.setMessage(b ? "Viewing ME Interface" : "Viewing Pattern Multi-Tool");
		}
	}

	// Draws the screen
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	// Draws the foreground
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.format("item.nae2.pattern_multiplier.name"), 8, 6, 4210752);

		this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
	}

	// Draws the background
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
		this.mc.getTextureManager().bindTexture(loc);
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, 177, this.ySize);

		// Draw pattern rows.
		int installedCapacityUpgrades = containerPatternMultiTool.getInstalledCapacityUpgrades();
		for (int u = 0; u < installedCapacityUpgrades; u++) {
			this.drawTexturedModalRect(offsetX + 8, offsetY + 37 + u * 18, 8, 19, 18 * 9 - 1, 18 - 1);
		}

		// Draw the upgrade inventory depending on the size.
		// Throw because this should never happen.
		int upgradeInventorySize =
			Objects.requireNonNull(containerPatternMultiTool.getPatternMultiToolUpgradeInventory()).getSlots();
		if (upgradeInventorySize > 0) {
			this.drawTexturedModalRect(offsetX + 180, offsetY, 180, 0, 32, 32);
			for (int u = 1; u < upgradeInventorySize; u++) {
				this.drawTexturedModalRect(offsetX + 180, offsetY + 8 + u * 18 - 1, 180, 7, 32, 30);
			}
		}

		// Draw the network tool if present.
		if (this.hasToolbox()) {
			this.drawTexturedModalRect(offsetX + 178, offsetY + this.ySize - 90, 178, this.ySize - 90, 68, 68);
		}
	}

	protected boolean hasToolbox() {
		return ((ContainerPatternMultiTool) this.inventorySlots).hasToolbox();
	}

	protected ContainerPatternMultiTool getContainer() {
		return (ContainerPatternMultiTool) this.inventorySlots;
	}

	@Override
	public int getPMTOffsetX() {
		return 0;
	}

	@Override
	public int getPMTOffsetY() {return 0;}

}

