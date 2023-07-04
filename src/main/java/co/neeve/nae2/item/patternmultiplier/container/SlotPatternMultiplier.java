package co.neeve.nae2.item.patternmultiplier.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotPatternMultiplier extends SlotRestrictedInput {
    private boolean allowEdit = true;
    private boolean isAllowEdit() {
        return this.allowEdit;
    }
    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }
    public SlotPatternMultiplier(IItemHandler i, int slotIndex, int x, int y, InventoryPlayer p) {
        super(PlacableItemType.PATTERN, i, slotIndex, x, y, p);

        this.setReturnAsSingleStack(false);
    }

    @Override
    public boolean isItemValid(ItemStack i) {
        if (!this.getContainer().isValidForSlot(this, i)) {
            return false;
        } else if (i.isEmpty()) {
            return false;
        } else if (i.getItem() == Items.AIR) {
            return false;
        } else if (!this.isAllowEdit()) {
            return false;
        } else {
            IDefinitions definitions = AEApi.instance().definitions();
            IMaterials materials = definitions.materials();
            if (i.getItem() instanceof ICraftingPatternItem) {
                return true;
            }

            return materials.blankPattern().isSameAs(i);
        }
    }
    public ItemStack getDisplayStack() {
        if (!Platform.isClient()) return super.getStack();

        ItemStack is = super.getStack();

        if (is.getItem() instanceof ItemEncodedPattern) {
            if (!is.isEmpty() && is.getItem() instanceof ItemEncodedPattern iep) {
                ItemStack out = iep.getOutput(is);

                if (!out.isEmpty()) {
                    return out;
                }
            }
        }

        return super.getStack();
    }
}
