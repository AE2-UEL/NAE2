package co.neeve.nae2.item.patternmultiplier.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.container.slot.OptionalSlotRestrictedInput;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotPatternMultiplier extends OptionalSlotRestrictedInput {
    private final IPatternMultiplierSlotHost host;
    private boolean allowEdit = true;

    public SlotPatternMultiplier(IItemHandler i, IPatternMultiplierSlotHost host, int slotIndex, int x, int y, int grp, InventoryPlayer p) {
        super(host.getContainerObject().isBoundToInterface() ?
                PlacableItemType.ENCODED_PATTERN : PlacableItemType.PATTERN, i, host, slotIndex, x, y, grp, p);
        this.host = host;

        this.setReturnAsSingleStack(false);
    }

    private boolean isAllowEdit() {
        return this.allowEdit;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    @Override
    public boolean isItemValid(ItemStack i) {
        if (this.host.getContainerObject().isBoundToInterface()) {
            return super.isItemValid(i);
        }

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
