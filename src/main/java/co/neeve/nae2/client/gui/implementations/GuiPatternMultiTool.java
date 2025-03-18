package co.neeve.nae2.client.gui.implementations;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.PartItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.IJEITargetSlot;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.gui.buttons.PMTSwitcherButton;
import co.neeve.nae2.client.gui.buttons.PMTTabButton;
import co.neeve.nae2.client.gui.buttons.PatternMultiToolButton;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.common.containers.ContainerPatternMultiTool;
import co.neeve.nae2.common.enums.PatternMultiToolActionTypes;
import co.neeve.nae2.common.enums.PatternMultiToolActions;
import co.neeve.nae2.common.enums.PatternMultiToolInventories;
import co.neeve.nae2.common.enums.PatternMultiToolTabs;
import co.neeve.nae2.common.items.patternmultitool.ObjPatternMultiTool;
import co.neeve.nae2.common.net.messages.PatternMultiToolPacket;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiPatternMultiTool extends AEBaseGui implements IPatternMultiToolHostGui, IJEIGhostIngredients {
	private static final ItemStack INTERFACE_ICON =
		AEApi.instance().definitions().blocks().iface().maybeStack(1).orElse(ItemStack.EMPTY);
	private static final ItemStack PMT_ICON =
		NAE2.definitions().items().patternMultiTool().maybeStack(1).orElse(ItemStack.EMPTY);
	// GUI texture
	private static final ResourceLocation loc = new ResourceLocation(Tags.MODID, "textures/gui/pattern_multiplier" +
		".png");
	private final Map<IGhostIngredientHandler.Target<?>, Object> mapTargetSlot = new HashMap<>();
	private final ContainerPatternMultiTool containerPMT;
	private PMTSwitcherButton switcherButton;
	private Rectangle tabSwitcherExclusion = null;
	private PatternMultiToolButton replace;

	// Constructor
	public GuiPatternMultiTool(InventoryPlayer inventoryPlayer, ObjPatternMultiTool te) {
		super(new ContainerPatternMultiTool(inventoryPlayer, te));
		this.containerPMT = (ContainerPatternMultiTool) this.inventorySlots;
		this.ySize = 189 + 18;
		this.xSize = 211;
	}

	// Handles button actions
	@Override
	protected void actionPerformed(@NotNull GuiButton btn) throws IOException {
		super.actionPerformed(btn);

		if (btn == this.switcherButton) {
			NAE2.net().sendToServer(new PatternMultiToolPacket(PatternMultiToolActionTypes.BUTTON_PRESS,
				PatternMultiToolActions.INV_SWITCH.ordinal()));
		} else if (btn instanceof PMTTabButton tabButton) {
			NAE2.net().sendToServer(new PatternMultiToolPacket(PatternMultiToolActionTypes.TAB_SWITCH,
				tabButton.getTab().ordinal()));
		}
	}

	// Initializes the GUI
	@Override
	public void initGui() {
		super.initGui();

		this.buttonList.clear();

		PMTTabButton first = null;
		for (var tab : PatternMultiToolTabs.values()) {
			var btn = new PMTTabButton(this, tab, this.guiLeft, this.guiTop + (tab.ordinal() * 19) + 4);
			if (first == null) {
				first = btn;
			}

			this.buttonList.add(btn);
		}

		var last = this.buttonList.get(this.buttonList.size() - 1);
		this.tabSwitcherExclusion = new Rectangle(first.x, first.y, last.width + last.x - first.x,
			last.height + last.y - first.y);

		this.setupTabSpecificButtons();

		// Draw Interface/PMT switcher
		if (this.getContainer().isBoundToInterface()) {
			this.switcherButton = new PMTSwitcherButton(this.guiLeft + 152 - 1, this.guiTop + 3 - 18 + 11, PMT_ICON,
				GuiText.Priority.getLocal(), this.itemRender);
			this.switcherButton.setHideEdge(1);
			this.switcherButton.id = 7;
			this.updateSwitcher();
			this.buttonList.add(this.switcherButton);
		}
	}

	public void setupTabSpecificButtons() {
		this.buttonList.removeIf(btn -> btn instanceof PatternMultiToolButton);

		// Calculate start position for buttons
		var start = 7 + this.guiLeft;

		var y = this.guiTop + 76 + 16;
		if (this.getContainer().getViewingTab() == PatternMultiToolTabs.MULTIPLIER) {
			// Add buttons to the GUI
			this.buttonList.add(new PatternMultiToolButton(start, y,
				PatternMultiToolActions.MUL2));
			this.buttonList.add(new PatternMultiToolButton(start + 23, y,
				PatternMultiToolActions.MUL3));
			this.buttonList.add(new PatternMultiToolButton(start + 46, y,
				PatternMultiToolActions.ADD));

			GuiButton unencode;
			this.buttonList.add(unencode = new PatternMultiToolButton(start + 176 - 60 - 15, y,
				PatternMultiToolActions.CLEAR));
			unencode.width = 60;
		} else if (this.getContainer().getViewingTab() == PatternMultiToolTabs.SEARCH_REPLACE) {
			this.replace = new PatternMultiToolButton(start + 176 - 60 - 15, y,
				PatternMultiToolActions.REPLACE);
			this.replace.width = 60;
			this.buttonList.add(this.replace);
		}
	}

	@Override
	public List<Rectangle> getJEIExclusionArea() {
		var list = new ArrayList<>(super.getJEIExclusionArea());
		if (this.tabSwitcherExclusion != null)
			list.add(this.tabSwitcherExclusion);
		return list;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		this.updateSwitcher();
	}

	private void updateSwitcher() {
		if (this.switcherButton != null) {
			var b = this.getContainer().viewingInventory == PatternMultiToolInventories.INTERFACE;
			var icon = this.getIcon(b);
			this.switcherButton.setItem(icon);
			this.switcherButton.setMessage(b ? "Viewing " + icon.getDisplayName() : "Viewing Pattern Multi-Tool");
		}
	}

	private ItemStack getIcon(boolean b) {
		if (b) {
			var obj = this.getContainer().getPatternMultiToolObject();
			if (obj != null) {
				var iface = obj.getInterface();
				if (iface instanceof IPart part) {
					return part.getItemStack(PartItemStack.NETWORK);
				}
			}
			return INTERFACE_ICON;
		}
		return PMT_ICON;
	}

	// Draws the foreground
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.format("item.nae2.pattern_multiplier.name"), 8, 6, 4210752);

		this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

		if (this.replace != null) {
			this.replace.enabled = false;
		}

		for (var entry :
			this.getContainer().getHighlightedSlots().entrySet()) {
			var slot = entry.getKey();
			var result = entry.getValue();
			var x = slot.xPos;
			var y = slot.yPos;
			var isOK = result == ContainerPatternMultiTool.ValidatonResult.OK;
			drawRect(x, y, x + 16, y + 16, isOK ? -1979646208 : 0xFFFF0000);

			if (isOK && this.replace != null) {
				this.replace.enabled = true;
			}

		}
	}

	// Draws the background
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
		this.mc.getTextureManager().bindTexture(loc);
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, 177, this.ySize);

		// Draw pattern rows.
		var installedCapacityUpgrades = this.getContainer().getInstalledCapacityUpgrades();
		for (var u = 0; u < installedCapacityUpgrades; u++) {
			this.drawTexturedModalRect(offsetX + 8, offsetY + 37 + u * 18, 8, 19, 18 * 9 - 1, 18 - 1);
		}

		// Draw the upgrade inventory depending on the size.
		// Throw because this should never happen.
		var upgradeInventorySize =
			Objects.requireNonNull(this.getContainer().getUpgradeInventory()).getSlots();
		if (upgradeInventorySize > 0) {
			this.drawTexturedModalRect(offsetX + 180, offsetY, 180, 0, 32, 32);
			for (var u = 1; u < upgradeInventorySize; u++) {
				this.drawTexturedModalRect(offsetX + 180, offsetY + 8 + u * 18 - 1, 180, 7, 32, 26);
			}
		}

		if (this.getContainer().getViewingTab() == PatternMultiToolTabs.SEARCH_REPLACE) {
			var slots = this.getContainer().getSearchReplaceSlots();
			if (slots != null) {
				for (var slot : slots) {
					this.drawTexturedModalRect(offsetX + slot.xPos - 1, offsetY + slot.yPos - 1, 8 - 1, 19 - 1, 18,
						18);
				}

				this.drawTexturedModalRect(offsetX + 8 + 3 + 18 - 1, offsetY + 76 + 19, 187, 35, 22, 15);
			}
		}

		// Draw the network tool if present.
		if (this.hasToolbox()) {
			this.drawTexturedModalRect(offsetX + 178, offsetY + this.ySize - 90, 178, this.ySize - 90, 68, 68);
		}
	}

	protected boolean hasToolbox() {
		return this.getContainer().hasToolbox();
	}

	protected ContainerPatternMultiTool getContainer() {
		return this.containerPMT;
	}

	@Override
	public int getPMTOffsetX() {
		return 0;
	}

	@Override
	public int getPMTOffsetY() {return 0;}

	// Shamelessly ported over from AE2. Code owned by respective copyright holders.
	@Override
	public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
		this.mapTargetSlot.clear();

		FluidStack fluidStack = null;
		var itemStack = ItemStack.EMPTY;

		if (ingredient instanceof ItemStack) {
			itemStack = (ItemStack) ingredient;
			fluidStack = FluidUtil.getFluidContained(itemStack);
		} else if (ingredient instanceof FluidStack) {
			fluidStack = (FluidStack) ingredient;
		}

		if (!(ingredient instanceof ItemStack) && !(ingredient instanceof FluidStack)) {
			return Collections.emptyList();
		}

		List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();

		List<IJEITargetSlot> slots = new ArrayList<>();
		if (!this.inventorySlots.inventorySlots.isEmpty()) {
			for (var slot : this.inventorySlots.inventorySlots) {
				if (slot instanceof SlotFake && (!itemStack.isEmpty())) {
					slots.add((IJEITargetSlot) slot);
				}
			}
		}
		if (!this.getGuiSlots().isEmpty()) {
			for (var slot : this.getGuiSlots()) {
				if (slot instanceof GuiFluidSlot && fluidStack != null) {
					slots.add((IJEITargetSlot) slot);
				}
			}
		}
		for (var slot : slots) {
			var targetItem = this.getTarget(slot, fluidStack, itemStack);
			targets.add(targetItem);
			this.mapTargetSlot.putIfAbsent(targetItem, slot);
		}
		return targets;
	}

	private IGhostIngredientHandler.@NotNull Target<Object> getTarget(IJEITargetSlot slot, FluidStack fluidStack,
	                                                                  ItemStack finalItemStack) {
		return new IGhostIngredientHandler.Target<>() {
			@Override
			public @NotNull Rectangle getArea() {
				if (slot instanceof SlotFake && ((SlotFake) slot).isSlotEnabled()) {
					return new Rectangle(GuiPatternMultiTool.this.getGuiLeft() + ((SlotFake) slot).xPos,
						GuiPatternMultiTool.this.getGuiTop() + ((SlotFake) slot).yPos, 16, 16);
				} else if (slot instanceof GuiFluidSlot && ((GuiFluidSlot) slot).isSlotEnabled()) {
					return new Rectangle(GuiPatternMultiTool.this.getGuiLeft() + ((GuiFluidSlot) slot).xPos(),
						GuiPatternMultiTool.this.getGuiTop() + ((GuiFluidSlot) slot).yPos(), 16, 16);
				}
				return new Rectangle();
			}

			@Override
			public void accept(@NotNull Object ingredient) {
				PacketInventoryAction p = null;
				try {
					if (slot instanceof SlotFake && ((SlotFake) slot).isSlotEnabled()) {
						if (finalItemStack.isEmpty() && fluidStack != null) {
							p = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, slot,
								AEItemStack.fromItemStack(FluidUtil.getFilledBucket(fluidStack)));
						} else if (!finalItemStack.isEmpty()) {
							p = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, slot,
								AEItemStack.fromItemStack(finalItemStack));
						}
					} else {
						if (fluidStack == null) {
							return;
						}
						p = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, slot,
							AEItemStack.fromItemStack(AEFluidStack.fromFluidStack(fluidStack).asItemStackRepresentation()));
					}
					NetworkHandler.instance().sendToServer(p);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public Map<IGhostIngredientHandler.Target<?>, Object> getFakeSlotTargetMap() {
		return this.mapTargetSlot;
	}
}

