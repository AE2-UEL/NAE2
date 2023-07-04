package co.neeve.nae2.item.patternmultiplier;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ObjPatternMultiplier implements IGuiItemObject, IAEAppEngInventory {
    private final AppEngInternalInventory inv;
    private final ItemStack is;

    public ObjPatternMultiplier(ItemStack is) {
        this.is = is;
        this.inv = new AppEngInternalInventory(this, 27);
        this.inv.setFilter(new ObjPatternMultiplier.ObjPatternMultiplierInventoryFilter());
        if (is.hasTagCompound()) {
            this.inv.readFromNBT(Platform.openNbtData(is), "inv");
        }

    }

    public void saveChanges() {
        this.inv.writeToNBT(Platform.openNbtData(this.is), "inv");
    }

    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
    }

    public ItemStack getItemStack() {
        return this.is;
    }

    public IItemHandler getInventory() {
        return this.inv;
    }

    private static class ObjPatternMultiplierInventoryFilter implements IAEItemFilter {
        private ObjPatternMultiplierInventoryFilter() {
        }

        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return stack.getItem() instanceof ItemEncodedPattern;
        }
    }
}
