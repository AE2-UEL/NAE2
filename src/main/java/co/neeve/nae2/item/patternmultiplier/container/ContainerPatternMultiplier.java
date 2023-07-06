package co.neeve.nae2.item.patternmultiplier.container;

import appeng.api.config.Upgrades;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.contents.NetworkToolViewer;
import appeng.items.tools.ToolNetworkTool;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.item.patternmultiplier.ObjPatternMultiplier;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class ContainerPatternMultiplier extends AEBaseContainer implements IAEAppEngInventory, IPatternMultiplierSlotHost {

    // Instance of ObjPatternMultiplier
    private final ObjPatternMultiplier toolInv;
    private NetworkToolViewer tbInventory;

    // Constructor
    public ContainerPatternMultiplier(InventoryPlayer ip, ObjPatternMultiplier te) {
        super(ip, te);
        this.toolInv = te;
        this.lockPlayerInventorySlot(ip.currentItem);

        // Add slots for the container
        addSlots();

        // Setup upgrade slots
        setupUpgrades();

        IInventory pi = this.getPlayerInv();
        World w = ip.player.world;

        int v;
        for(v = 0; v < pi.getSizeInventory(); ++v) {
            ItemStack pii = pi.getStackInSlot(v);
            if (!pii.isEmpty() && pii.getItem() instanceof ToolNetworkTool) {
                this.lockPlayerInventorySlot(v);
                this.tbInventory = (NetworkToolViewer)((IGuiItem)pii.getItem()).getGuiObject(pii, w, new BlockPos(0, 0, 0));
                break;
            }
        }

        if (this.hasToolbox()) {
            for(v = 0; v < 3; ++v) {
                for(int u = 0; u < 3; ++u) {
                    this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, this.tbInventory.getInternalInventory(), u + v * 3, 186 + u * 18, 189 + 18 - 82 + v * 18, this.getInventoryPlayer())).setPlayerSide());
                }
            }
        }

        // Bind the player's inventory
        this.bindPlayerInventory(ip, 0, 107 + 18);
    }

    public boolean hasToolbox() {
        return this.tbInventory != null;
    }

    public AppEngInternalInventory getPatternInventory() {
        return (AppEngInternalInventory) this.toolInv.getInventory();
    }

    // Add slots for the container
    private void addSlots() {
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 9; ++x) {
                SlotRestrictedInput slot = new SlotPatternMultiplier(this.toolInv.getInventory(), this,
                        y * 9 + x, 8 + x * 18, 19 + y * 18, y, this.getInventoryPlayer());
                slot.setStackLimit(64);

                this.addSlotToContainer(slot);
            }
        }
    }

    public void setupUpgrades() {
        if (this.toolInv != null) {
            UpgradeInventory ui = (UpgradeInventory) this.toolInv.getInventoryByName("upgrades");

            for (int upgradeSlot = 0; upgradeSlot < ui.getSlots(); upgradeSlot++) {
                SlotRestrictedInput slot = new SlotPatternMultiplierUpgrade(SlotRestrictedInput.PlacableItemType.UPGRADES,
                        ui, this, upgradeSlot, 187, 8 + upgradeSlot * 18, this.getInventoryPlayer());
                slot.setNotDraggable();
                this.addSlotToContainer(slot);
            }
        }
    }

    // Check changes and send updates
    @Override
    public void detectAndSendChanges() {
        ItemStack currentItem = this.getPlayerInv().getCurrentItem();
        ItemStack toolInvItemStack = this.toolInv.getItemStack();

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
        super.detectAndSendChanges();
    }

    @Override
    public void saveChanges() {
        if (Platform.isServer()) {
            this.toolInv.saveChanges();
        }
    }

    @Override
    public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack, ItemStack itemStack1) {

    }

    @Override
    public boolean isSlotEnabled(int i) {
        return i <= ((UpgradeInventory)this.toolInv.getInventoryByName("upgrades")).getInstalledUpgrades(Upgrades.CAPACITY);
    }

    @Override
    public boolean canTakeStack(SlotPatternMultiplierUpgrade slot, EntityPlayer player) {
        List<ItemStack> inventory = Lists.newArrayList((AppEngInternalInventory) this.toolInv.getInventory());
        int slices = inventory.size() / 9;
        UpgradeInventory upgrades = (UpgradeInventory) this.toolInv.getInventoryByName("upgrades");

        int lockedUpgrades = slices; // Stub
        int installedUpgrades = upgrades.getInstalledUpgrades(Upgrades.CAPACITY);

        for (int i = slices - 1; i >= 0; i--) {
            if (inventory.subList(i * 9, i * 9 + 9).stream().allMatch(ItemStack::isEmpty)) {
                lockedUpgrades--;
            } else {
                break;
            }
        }

        return installedUpgrades >= lockedUpgrades;
    }
}

