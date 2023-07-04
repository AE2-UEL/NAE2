package co.neeve.nae2.item.patternmultiplier.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import co.neeve.nae2.item.patternmultiplier.ObjPatternMultiplier;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPatternMultiplier extends AEBaseContainer {

    // Instance of ObjPatternMultiplier
    private final ObjPatternMultiplier toolInv;

    // Constructor
    public ContainerPatternMultiplier(InventoryPlayer ip, ObjPatternMultiplier te) {
        super(ip, null, null);
        this.toolInv = te;
        this.lockPlayerInventorySlot(ip.currentItem);

        // Add slots for the container
        addSlots(te);

        // Bind the player's inventory
        this.bindPlayerInventory(ip, 0, 107);
    }

    // Add slots for the container
    private void addSlots(ObjPatternMultiplier te) {
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                SlotRestrictedInput slot = new SlotPatternMultiplier(te.getInventory(),
                        y * 9 + x, 8 + x * 18, 19 + y * 18, this.getInventoryPlayer());
                slot.setStackLimit(64);

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
}

