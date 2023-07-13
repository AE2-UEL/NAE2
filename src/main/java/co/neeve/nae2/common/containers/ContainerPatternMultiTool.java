package co.neeve.nae2.common.containers;

import appeng.api.config.Upgrades;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.IInterfaceHost;
import appeng.items.contents.NetworkToolViewer;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.tools.ToolNetworkTool;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.common.enums.PatternMultiToolInventories;
import co.neeve.nae2.common.interfaces.IContainerPatternMultiTool;
import co.neeve.nae2.common.slots.SlotPatternMultiTool;
import co.neeve.nae2.common.slots.SlotPatternMultiToolUpgrade;
import co.neeve.nae2.items.patternmultitool.ObjPatternMultiTool;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerPatternMultiTool extends AEBaseContainer implements IAEAppEngInventory,
	IContainerPatternMultiTool {
	// Instance of ObjPatternMultiTool
	private final ObjPatternMultiTool patternMultiTool;
	private final InventoryPlayer inventoryPlayer;
	private final List<AppEngSlot> patternMultiToolSlots = new ArrayList<>();
	private final IInterfaceHost iface;
	@GuiSync(0)
	public PatternMultiToolInventories viewingInventory = PatternMultiToolInventories.PMT;
	private NetworkToolViewer tbInventory;

	public ContainerPatternMultiTool(InventoryPlayer ip, ObjPatternMultiTool te, IInterfaceHost iface) {
		super(ip, te);
		this.patternMultiTool = te;
		this.inventoryPlayer = ip;
		this.iface = iface;
		this.lockPlayerInventorySlot(ip.currentItem);

		if (iface != null) {
			this.viewingInventory = PatternMultiToolInventories.INTERFACE;
		}

		// Add slots for the container
		addSlots();
	}

	public boolean hasToolbox() {
		return this.tbInventory != null;
	}

	public boolean isViewingInterface() {
		return this.viewingInventory == PatternMultiToolInventories.INTERFACE;
	}

	@Override
	public IItemHandler getPatternMultiToolInventory() {
		switch (this.viewingInventory) {
			case PMT -> {
				return this.patternMultiTool.getPatternInventory();
			}
			case INTERFACE -> {
				return this.iface.getInventoryByName("patterns");
			}
		}
		return null;
	}

	@Override
	public void onUpdate(String field, Object oldValue, Object newValue) {
		if (field.equals("viewingInventory")) {
			this.addSlots();
		}
	}


	// Add slots for the container
	private void addSlots() {
		this.inventorySlots.clear();
		this.inventoryItemStacks.clear();

		for (int y = 0; y < 4; ++y) {
			for (int x = 0; x < 9; ++x) {
				SlotPatternMultiTool slot = new SlotPatternMultiTool(this.getPatternMultiToolInventory(), this,
					y * 9 + x, 8 + x * 18, 19 + y * 18, y, this.getInventoryPlayer());
				slot.setStackLimit(this.viewingInventory == PatternMultiToolInventories.INTERFACE ? 1 : 64);

				this.addSlotToContainer(slot);
				this.patternMultiToolSlots.add(slot);
			}
		}

		// Setup upgrade slots
		setupUpgrades();

		IInventory pi = this.getPlayerInv();
		World w = inventoryPlayer.player.world;

		int v;
		for (v = 0; v < pi.getSizeInventory(); ++v) {
			ItemStack pii = pi.getStackInSlot(v);
			if (!pii.isEmpty() && pii.getItem() instanceof ToolNetworkTool) {
				this.lockPlayerInventorySlot(v);
				this.tbInventory = (NetworkToolViewer) ((IGuiItem) pii.getItem()).getGuiObject(pii, w, new BlockPos(0,
					0, 0));
				break;
			}
		}

		if (this.hasToolbox()) {
			for (v = 0; v < 3; ++v) {
				for (int u = 0; u < 3; ++u) {
					this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES,
						this.tbInventory.getInternalInventory(), u + v * 3, 186 + u * 18, 189 + 18 - 82 + v * 18,
						this.getInventoryPlayer())).setPlayerSide());
				}
			}
		}

		// Bind the player's inventory
		this.bindPlayerInventory(this.inventoryPlayer, 0, 107 + 18);
	}

	public UpgradeInventory getPatternMultiToolUpgradeInventory() {
		switch (this.viewingInventory) {
			case PMT -> {
				return this.patternMultiTool.getUpgradeInventory();
			}
			case INTERFACE -> {
				return (UpgradeInventory) this.iface.getInventoryByName("upgrades");
			}
		}
		return null;
	}

	public boolean isBoundToInterface() {
		return this.iface != null;
	}

	public void setupUpgrades() {
		UpgradeInventory ui = this.getPatternMultiToolUpgradeInventory();

		// Throw because this should never happen.
		for (int upgradeSlot = 0; upgradeSlot < Objects.requireNonNull(ui).getSlots(); upgradeSlot++) {
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
			TileEntity tileEntity = iface.getTileEntity();
			if (tileEntity == null || tileEntity != tileEntity.getWorld().getTileEntity(tileEntity.getPos())) {
				this.setValidContainer(false);
				super.detectAndSendChanges();
				return;
			}
		}

		ItemStack currentItem = this.getPlayerInv().getCurrentItem();
		ItemStack toolInvItemStack = this.patternMultiTool.getItemStack();

		if (!ItemStack.areItemsEqual(toolInvItemStack, currentItem)) {
			if (!currentItem.isEmpty()) {
				this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, toolInvItemStack);
			}
			this.setValidContainer(false);
			super.detectAndSendChanges();
			return;
		}

		super.detectAndSendChanges();
	}

	// Handle slot changes
	public void onSlotChange(Slot s) {
		if (this.viewingInventory == PatternMultiToolInventories.INTERFACE) {
			IItemHandler patterns = this.getPatternMultiToolInventory();

			List<ItemStack> dropList = new ArrayList<>();

			int maxSlots =
				((UpgradeInventory) iface.getInventoryByName("upgrades")).getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) * 9;

			// Throw because this should never happen.
			for (int invSlot = 0; invSlot < Objects.requireNonNull(patterns).getSlots(); ++invSlot) {
				ItemStack is = patterns.getStackInSlot(invSlot);
				if (!(is.getItem() instanceof ItemEncodedPattern)) {
					dropList.add(patterns.extractItem(invSlot, Integer.MAX_VALUE, false));
				} else if (invSlot > 8 + maxSlots) {
					if (!is.isEmpty()) {
						dropList.add(patterns.extractItem(invSlot, Integer.MAX_VALUE, false));
					}
				}
			}

			if (dropList.size() > 0) {
				TileEntity tileEntity = iface.getTileEntity();
				World world = tileEntity.getWorld();
				BlockPos blockPos = tileEntity.getPos();
				Platform.spawnDrops(world, blockPos, dropList);
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
	public boolean isPatternMultiToolSlotEnabled(int i) {
		return i <= this.getInstalledCapacityUpgrades();
	}

	public int getInstalledCapacityUpgrades() {
		Upgrades which = null;
		UpgradeInventory ui = this.getPatternMultiToolUpgradeInventory();

		switch (this.viewingInventory) {
			case PMT -> which = Upgrades.CAPACITY;
			case INTERFACE -> which = Upgrades.PATTERN_EXPANSION;
		}
		if (which == null) {
			throw new RuntimeException("Invalid upgrade container state.");
		}

		// Throw because this should never happen.
		return Objects.requireNonNull(ui).getInstalledUpgrades(which);
	}

	@Override
	public List<AppEngSlot> getPatternMultiToolSlots() {
		return this.patternMultiToolSlots;
	}

	@Override
	public boolean canTakeStack(SlotPatternMultiToolUpgrade slot, EntityPlayer player) {
		// Throw because this should never happen.
		List<ItemStack> inventory =
			Lists.newArrayList((AppEngInternalInventory) Objects.requireNonNull(this.getPatternMultiToolInventory()));
		int slices = inventory.size() / 9;
		int lockedUpgrades = slices; // Stub
		int installedUpgrades = this.getInstalledCapacityUpgrades();

		for (int i = slices - 1; i >= 0; i--) {
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
		viewingInventory = viewingInventory == PatternMultiToolInventories.PMT ?
			PatternMultiToolInventories.INTERFACE : PatternMultiToolInventories.PMT;
		addSlots();
	}
}

