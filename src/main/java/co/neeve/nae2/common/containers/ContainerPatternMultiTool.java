package co.neeve.nae2.common.containers;

import appeng.api.config.Upgrades;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.ItemStackHelper;
import appeng.items.contents.NetworkToolViewer;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.tools.ToolNetworkTool;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.client.gui.implementations.GuiPatternMultiTool;
import co.neeve.nae2.common.enums.PatternMultiToolInventories;
import co.neeve.nae2.common.enums.PatternMultiToolTabs;
import co.neeve.nae2.common.interfaces.IContainerPatternMultiTool;
import co.neeve.nae2.common.items.patternmultitool.ObjPatternMultiTool;
import co.neeve.nae2.common.slots.SlotPatternMultiTool;
import co.neeve.nae2.common.slots.SlotPatternMultiToolUpgrade;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContainerPatternMultiTool extends AEBaseContainer implements IAEAppEngInventory,
	IContainerPatternMultiTool {
	// Instance of ObjPatternMultiTool
	private final ObjPatternMultiTool patternMultiTool;
	private final InventoryPlayer inventoryPlayer;
	private final List<AppEngSlot> patternMultiToolSlots = new ArrayList<>();
	private final IInterfaceHost iface;
	@GuiSync(0)
	public PatternMultiToolInventories viewingInventory = PatternMultiToolInventories.PMT;
	@GuiSync(1)
	public PatternMultiToolTabs viewingTab;
	@SideOnly(Side.CLIENT)
	private HashMap<AppEngSlot, ValidatonResult> highlightedSlots;
	private NetworkToolViewer tbInventory;
	private List<SlotFake> srSlots;

	public ContainerPatternMultiTool(InventoryPlayer ip, ObjPatternMultiTool te) {
		super(ip, te);
		this.patternMultiTool = te;
		this.inventoryPlayer = ip;
		this.lockPlayerInventorySlot(ip.currentItem);
		this.iface = te.getInterface();
		if (this.iface != null) {
			this.viewingInventory = PatternMultiToolInventories.INTERFACE;
		}
		this.viewingTab = te.getTab();

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			this.highlightedSlots = new HashMap<>();
		}

		// Add slots for the container
		this.addSlots();
	}

	public HashMap<AppEngSlot, ValidatonResult> getHighlightedSlots() {
		return this.highlightedSlots;
	}

	public List<SlotFake> getSearchReplaceSlots() {
		return this.srSlots;
	}

	public boolean hasToolbox() {
		return this.tbInventory != null;
	}

	public boolean isViewingInterface() {
		return this.viewingInventory == PatternMultiToolInventories.INTERFACE;
	}

	@Override
	@Nonnull
	public IItemHandler getPatternInventory() {
		IItemHandler result = null;
		switch (this.viewingInventory) {
			case PMT -> result = this.patternMultiTool.getPatternInventory();
			case INTERFACE -> result = this.iface.getInventoryByName("patterns");
		}
		return result;
	}

	@Override
	public void onUpdate(String field, Object oldValue, Object newValue) {
		if (field.equals("viewingInventory") || field.equals("viewingTab")) {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				if (Minecraft.getMinecraft().currentScreen instanceof GuiPatternMultiTool gpmt) {
					if (field.equals("viewingTab")) {
						assert this.getPatternMultiToolObject() != null;
						this.getPatternMultiToolObject().setTab((PatternMultiToolTabs) newValue);
					}
					gpmt.setupTabSpecificButtons();
				}
			}

			this.addSlots();
		}
	}

	// Add slots for the container
	private void addSlots() {
		this.inventorySlots.clear();
		this.inventoryItemStacks.clear();
		this.patternMultiToolSlots.clear();
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			this.highlightedSlots.clear();
		}
		this.srSlots = null;

		for (var y = 0; y < 4; ++y) {
			for (var x = 0; x < 9; ++x) {
				var slot = new SlotPatternMultiTool(this.getPatternInventory(), this,
					y * 9 + x, 8 + x * 18, 19 + y * 18, y, this.getInventoryPlayer());
				slot.setStackLimit(this.viewingInventory == PatternMultiToolInventories.INTERFACE ? 1 : 64);

				this.addSlotToContainer(slot);
				this.patternMultiToolSlots.add(slot);
			}
		}

		// Setup upgrade slots
		this.setupUpgrades();

		IInventory pi = this.getPlayerInv();
		var w = this.inventoryPlayer.player.world;

		int v;
		for (v = 0; v < pi.getSizeInventory(); ++v) {
			var pii = pi.getStackInSlot(v);
			if (!pii.isEmpty() && pii.getItem() instanceof ToolNetworkTool) {
				this.lockPlayerInventorySlot(v);
				this.tbInventory = (NetworkToolViewer) ((IGuiItem) pii.getItem()).getGuiObject(pii, w, new BlockPos(0,
					0, 0));
				break;
			}
		}

		if (this.getViewingTab() == PatternMultiToolTabs.SEARCH_REPLACE) {
			this.srSlots = new ArrayList<>();
			var inv = this.patternMultiTool.getSearchReplaceInventory();
			this.srSlots.add((SlotFake) this.addSlotToContainer(new SlotFakeTypeOnly(inv, 0, 8, 76 + 18 + 1)));
			this.srSlots.add((SlotFake) this.addSlotToContainer(new SlotFakeTypeOnly(inv, 1, 8 + 6 + 22 + 18,
				76 + 18 + 1)));
		}

		if (this.hasToolbox()) {
			for (v = 0; v < 3; ++v) {
				for (var u = 0; u < 3; ++u) {
					this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES,
						this.tbInventory.getInternalInventory(), u + v * 3, 186 + u * 18, 189 + 18 - 82 + v * 18,
						this.getInventoryPlayer())).setPlayerSide());
				}
			}
		}

		// Bind the player's inventory
		this.bindPlayerInventory(this.inventoryPlayer, 0, 107 + 18);
	}

	@Nonnull
	public UpgradeInventory getUpgradeInventory() {
		UpgradeInventory result = null;
		switch (this.viewingInventory) {
			case PMT -> result = this.patternMultiTool.getUpgradeInventory();
			case INTERFACE -> result = (UpgradeInventory) this.iface.getInventoryByName("upgrades");
		}
		return result;
	}

	public boolean isBoundToInterface() {
		return this.iface != null;
	}

	public void setupUpgrades() {
		var ui = this.getUpgradeInventory();

		// Throw because this should never happen.
		for (var upgradeSlot = 0; upgradeSlot < ui.getSlots(); upgradeSlot++) {
			SlotRestrictedInput slot = new SlotPatternMultiToolUpgrade(SlotRestrictedInput.PlacableItemType.UPGRADES,
				ui, this, upgradeSlot, 187, 8 + upgradeSlot * 18, this.getInventoryPlayer());
			slot.setNotDraggable();
			this.addSlotToContainer(slot);
		}
	}

	// Check changes and send updates
	@Override
	public void detectAndSendChanges() {
		if (this.iface != null) {
			var tileEntity = this.iface.getTileEntity();
			if (tileEntity == null || tileEntity != tileEntity.getWorld().getTileEntity(tileEntity.getPos())) {
				this.setValidContainer(false);
				super.detectAndSendChanges();
				return;
			}
		}

		var currentItem = this.getPlayerInv().getCurrentItem();
		var toolInvItemStack = this.patternMultiTool.getItemStack();

		if (!ItemStack.areItemsEqual(toolInvItemStack, currentItem)) {
			if (!currentItem.isEmpty()) {
				this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, toolInvItemStack);
			}
			this.setValidContainer(false);
		}

		super.detectAndSendChanges();
	}

	// Handle slot changes
	public void onSlotChange(Slot s) {
		if (this.viewingInventory == PatternMultiToolInventories.INTERFACE) {
			var patterns = this.getPatternInventory();

			List<ItemStack> dropList = new ArrayList<>();

			var maxSlots =
				((UpgradeInventory) this.iface.getInventoryByName("upgrades")).getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) * 9;

			for (var invSlot = 0; invSlot < patterns.getSlots(); ++invSlot) {
				var is = patterns.getStackInSlot(invSlot);
				if (!(is.getItem() instanceof ICraftingPatternItem)) {
					dropList.add(patterns.extractItem(invSlot, Integer.MAX_VALUE, false));
				} else if (invSlot > 8 + maxSlots) {
					if (!is.isEmpty()) {
						dropList.add(patterns.extractItem(invSlot, Integer.MAX_VALUE, false));
					}
				}
			}

			if (!dropList.isEmpty()) {
				var tileEntity = this.iface.getTileEntity();
				var world = tileEntity.getWorld();
				var blockPos = tileEntity.getPos();
				Platform.spawnDrops(world, blockPos, dropList);
			}
		}

		if (this.getViewingTab() == PatternMultiToolTabs.SEARCH_REPLACE) {
			if (FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT) return;
			this.highlightedSlots.clear();

			var host = this;
			var srInv = host.getSearchReplaceInventory();
			if (srInv == null) return;

			var itemA = srInv.getStackInSlot(0);
			var itemB = srInv.getStackInSlot(1);
			if (itemA.isEmpty()) return;

			var itemBData = ItemStackHelper.stackToNBT(itemB);
			var crafting = new InventoryCrafting(new ContainerNull(), 3, 3);

			for (var slot : this.patternMultiToolSlots) {
				var is = slot.getStack();
				if (!(is.getItem() instanceof ItemEncodedPattern)) continue;
				var nbt = is.getTagCompound();
				if (nbt == null) {
					// Skip this item if it has no NBT data
					continue;
				}

				var ae2fc = Platform.isModLoaded("ae2fc") && is.getItem() instanceof ItemFluidEncodedPattern;
				final var countTag = ae2fc ? "Cnt" : "Count"; // ¯\_(ツ)_/¯

				var isCrafting = nbt.getBoolean("crafting");
				ValidatonResult result = null;

				final var tagIn = (NBTTagList) nbt.getTag("in").copy();
				final var tagOut = (NBTTagList) nbt.getTag("out").copy();

				var lists = new ArrayList<NBTTagList>();
				lists.add(tagIn);
				if (!isCrafting) lists.add(tagOut);

				var fluidStackIn = FluidUtil.getFluidContained(itemA);
				var fluidReplacement = ae2fc && fluidStackIn != null;

				for (var list : lists) {
					var idx = 0;
					for (var tag : list.copy()) {
						var compound = (NBTTagCompound) tag;
						var stack = ItemStackHelper.stackFromNBT(compound);
						if (itemA.isItemEqual(stack)) {
							result = ValidatonResult.OK;

							// If crafting, store validation for later.
							if (isCrafting) {
								var count = compound.getTag(countTag).copy();
								var data = itemBData.copy();
								data.setTag(countTag, count);
								list.set(idx, data);
							} else continue;
						} else if (fluidReplacement && stack.getItem() instanceof ItemFluidDrop) {
							FluidStack fluidStack = FakeItemRegister.getStack(stack);

							// This should never be a crafting pattern.
							if (fluidStackIn.isFluidEqual(fluidStack)) {
								result = ValidatonResult.OK;
							}
						}
						idx++;
					}
				}

				if (result != null) {
					// Validate
					if (isCrafting) {
						try {
							if (tagIn.tagCount() != 9) {
								result = ValidatonResult.ERROR;
							}
							var w = this.inventoryPlayer.player.world;

							crafting.clear();
							for (var j = 0; j < tagIn.tagCount(); j++) {
								var is1 = ItemStackHelper.stackFromNBT((NBTTagCompound) tagIn.get(j));
								crafting.setInventorySlotContents(j, is1);
							}

							if (null == CraftingManager.findMatchingRecipe(crafting, w)) {
								result = ValidatonResult.ERROR;
							}
						} catch (Exception e) {
							continue;
						}
					}

					this.highlightedSlots.put(slot, result);
				}
			}
		}


		super.detectAndSendChanges();
	}

	@Override
	public void saveChanges() {
		if (Platform.isServer()) {
			this.patternMultiTool.saveChanges();
		}
	}

	@Override
	public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack,
	                              ItemStack itemStack1) {

	}

	@Override
	public boolean isPatternRowEnabled(int i) {
		return i <= this.getInstalledCapacityUpgrades();
	}

	public int getInstalledCapacityUpgrades() {
		Upgrades which = null;
		var ui = this.getUpgradeInventory();

		switch (this.viewingInventory) {
			case PMT -> which = Upgrades.CAPACITY;
			case INTERFACE -> which = Upgrades.PATTERN_EXPANSION;
		}

		// Throw because this should never happen.
		return ui.getInstalledUpgrades(which);
	}

	@Override
	public boolean canTakeStack() {
		// Throw because this should never happen.
		List<ItemStack> inventory =
			Lists.newArrayList((AppEngInternalInventory) this.getPatternInventory());
		var slices = inventory.size() / 9;
		var lockedUpgrades = slices; // Stub
		var installedUpgrades = this.getInstalledCapacityUpgrades();

		for (var i = slices - 1; i >= 0; i--) {
			if (inventory.subList(i * 9, i * 9 + 9).stream().allMatch(ItemStack::isEmpty)) {
				lockedUpgrades--;
			} else {
				break;
			}
		}

		return installedUpgrades >= lockedUpgrades;
	}

	@Override
	public ObjPatternMultiTool getPatternMultiToolObject() {
		return this.patternMultiTool;
	}

	public void toggleInventory() {
		this.viewingInventory = this.viewingInventory == PatternMultiToolInventories.PMT ?
			PatternMultiToolInventories.INTERFACE : PatternMultiToolInventories.PMT;
		this.addSlots();
	}

	public void switchTab(PatternMultiToolTabs tab) {
		var patternMultiToolObject = this.getPatternMultiToolObject();
		assert patternMultiToolObject != null;
		patternMultiToolObject.setTab(tab);
		patternMultiToolObject.saveChanges();

		// For sync purposes.
		this.viewingTab = tab;

		this.addSlots();
		this.detectAndSendChanges();
	}

	public PatternMultiToolTabs getViewingTab() {
		assert this.getPatternMultiToolObject() != null;
		return this.getPatternMultiToolObject().getTab();
	}

	public enum ValidatonResult {
		OK,
		ERROR
	}
}

