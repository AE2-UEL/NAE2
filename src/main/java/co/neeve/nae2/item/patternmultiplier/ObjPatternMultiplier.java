package co.neeve.nae2.item.patternmultiplier;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class ObjPatternMultiplier implements IGuiItemObject, IAEAppEngInventory {
    private final AppEngInternalInventory inv;
    private final ItemStack is;

    public static final int UPGRADE_COUNT = 3;

    private StackUpgradeInventory upgrades;

    public ObjPatternMultiplier(ItemStack is) {
        this.is = is;
        this.inv = new AppEngInternalInventory(this, 36);
        this.inv.setFilter(new ObjPatternMultiplier.ObjPatternMultiplierInventoryFilter());
        upgrades = new StackUpgradeInventory(this.is, this, UPGRADE_COUNT);

        if (is.hasTagCompound()) {
            NBTTagCompound data = Platform.openNbtData(is);
            data.getCompoundTag("inv").removeTag("Size");
            data.getCompoundTag("upgrades").removeTag("Size");

            this.inv.readFromNBT(data, "inv");
            this.upgrades.readFromNBT(data, "upgrades");
        }


    }

    public void saveChanges() {
        NBTTagCompound data = Platform.openNbtData(this.is);

        this.inv.writeToNBT(data, "inv");
        this.upgrades.writeToNBT(data, "upgrades");
    }

    public void saveChanges(NBTTagCompound data) {
        if (is.getTagCompound() != null) {
            is.getTagCompound().merge(data);
        } else {
            is.setTagCompound(data);
        }
    }

    public IItemHandler getInventoryByName(String name) {
        if (name.equals("inv")) {
            return inv;
        } else if (name.equals("upgrades")) {
            return upgrades;
        }
        return null;
    }

    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
    }

    public ItemStack    getItemStack() {
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


