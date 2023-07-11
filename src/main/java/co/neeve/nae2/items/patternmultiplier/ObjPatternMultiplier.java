package co.neeve.nae2.items.patternmultiplier;

import appeng.api.config.Upgrades;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.helpers.IInterfaceHost;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import co.neeve.nae2.common.enums.PatternMultiplierInventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class ObjPatternMultiplier implements IGuiItemObject, IAEAppEngInventory {
    public static final int UPGRADE_COUNT = 3;
    private final AppEngInternalInventory inv;
    private final ItemStack is;
    private final StackUpgradeInventory upgrades;
    private IInterfaceHost iface = null;

    public ObjPatternMultiplier(ItemStack is, IInterfaceHost iface) {
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

        if (iface != null) {
            this.iface = iface;
        }
    }

    public void saveChanges() {
        NBTTagCompound data = Platform.openNbtData(this.is);

        this.inv.writeToNBT(data, "inv");
        this.upgrades.writeToNBT(data, "upgrades");
    }

    public boolean isBoundToInterface() {
        return this.iface != null;
    }

    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
    }

    public ItemStack getItemStack() {
        return this.is;
    }

    public IItemHandler getPatternInventory(PatternMultiplierInventories which) {
        switch (which) {
            case PMT -> {
                return this.inv;
            }
            case INTERFACE -> {
                return this.iface.getInventoryByName("patterns");
            }
        }
        return null;
    }

    public UpgradeInventory getUpgradeInventory(PatternMultiplierInventories which) {
        switch (which) {
            case PMT -> {
                return this.upgrades;
            }
            case INTERFACE -> {
                return (UpgradeInventory) this.iface.getInventoryByName("upgrades");
            }
        }
        return null;
    }

    public int getInstalledCapacityUpgrades(PatternMultiplierInventories which) {
        UpgradeInventory inv = this.getUpgradeInventory(which);

        return inv.getInstalledUpgrades(which == PatternMultiplierInventories.INTERFACE ? Upgrades.PATTERN_EXPANSION : Upgrades.CAPACITY);
    }

    public IInterfaceHost getInterface() {
        return this.iface;
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


