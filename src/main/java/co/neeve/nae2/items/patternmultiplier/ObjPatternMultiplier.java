package co.neeve.nae2.items.patternmultiplier;

import appeng.api.config.Upgrades;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class ObjPatternMultiplier implements IGuiItemObject, IAEAppEngInventory {
    public static final int UPGRADE_COUNT = 3;
    private final AppEngInternalInventory inv;
    private final ItemStack is;
    private final StackUpgradeInventory upgrades;

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

    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
    }

    public ItemStack getItemStack() {
        return this.is;
    }

    public IItemHandler getPatternInventory() {
        return this.inv;
    }

    public UpgradeInventory getUpgradeInventory() {
        return this.upgrades;
    }

    public int getInstalledCapacityUpgrades() {
        return upgrades.getInstalledUpgrades(Upgrades.CAPACITY);
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


