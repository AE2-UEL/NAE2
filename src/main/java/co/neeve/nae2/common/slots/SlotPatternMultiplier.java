package co.neeve.nae2.common.slots;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;
import co.neeve.nae2.common.interfaces.IContainerPatternMultiplier;
import co.neeve.nae2.common.interfaces.IPatternMultiplierHost;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotPatternMultiplier extends SlotRestrictedInput {
    private final IPatternMultiplierHost host;
    private final int groupNum;
    private boolean allowEdit = true;

    public SlotPatternMultiplier(IItemHandler i, IPatternMultiplierHost host, int slotIndex, int x, int y, int grp, InventoryPlayer p) {
        super(host instanceof IContainerPatternMultiplier cmp && cmp.isViewingInterface() ?
                PlacableItemType.ENCODED_PATTERN : PlacableItemType.PATTERN, i, slotIndex, x, y, p);
        this.host = host;
        this.groupNum = grp;

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
        if (this.host instanceof IContainerPatternMultiplier cmp && cmp.isViewingInterface()) {
            return super.isItemValid(i);
        }

        if (!this.isSlotEnabled()) {
            return false;
        } else if (!this.getContainer().isValidForSlot(this, i)) {
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

    @Override
    public boolean isSlotEnabled() {
        return this.host != null && this.host.isPatternSlotEnabled(this.groupNum);
    }
}
