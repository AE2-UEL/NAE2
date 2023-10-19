package co.neeve.nae2.common.items.patternmultitool;

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
import co.neeve.nae2.common.enums.PatternMultiToolTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class ObjPatternMultiTool implements IGuiItemObject, IAEAppEngInventory {
	public static final int UPGRADE_COUNT = 3;
	private final AppEngInternalInventory inv;
	private final AppEngInternalInventory srInv;
	private final ItemStack is;
	private final StackUpgradeInventory upgrades;
	private PatternMultiToolTabs tab = PatternMultiToolTabs.MULTIPLIER;

	public ObjPatternMultiTool(ItemStack is) {
		this.is = is;
		this.inv = new AppEngInternalInventory(this, 36);
		this.srInv = new AppEngInternalInventory(this, 2, 1);
		this.inv.setFilter(new ObjPatternMultiToolInventoryFilter());
		upgrades = new StackUpgradeInventory(this.is, this, UPGRADE_COUNT);

		if (is.hasTagCompound()) {
			NBTTagCompound data = Platform.openNbtData(is);
			data.getCompoundTag("inv").removeTag("Size");
			data.getCompoundTag("upgrades").removeTag("Size");
			data.getCompoundTag("srInv").removeTag("Size");

			this.inv.readFromNBT(data, "inv");
			this.upgrades.readFromNBT(data, "upgrades");
			this.srInv.readFromNBT(data, "srInv");
			this.tab = PatternMultiToolTabs.fromInt(data.getInteger("tab"));
		}
	}

	public void saveChanges() {
		NBTTagCompound data = Platform.openNbtData(this.is);

		this.inv.writeToNBT(data, "inv");
		this.upgrades.writeToNBT(data, "upgrades");
		this.srInv.writeToNBT(data, "srInv");
		data.setInteger("tab", tab.ordinal());
	}

	public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack,
	                              ItemStack newStack) {
	}

	public ItemStack getItemStack() {
		return this.is;
	}

	public IItemHandler getPatternInventory() {
		return this.inv;
	}

	public IItemHandler getSearchReplaceInventory() {
		return this.srInv;
	}

	public UpgradeInventory getUpgradeInventory() {
		return this.upgrades;
	}

	public int getInstalledCapacityUpgrades() {
		return upgrades.getInstalledUpgrades(Upgrades.CAPACITY);
	}

	public PatternMultiToolTabs getTab() {
		return tab;
	}

	public void setTab(PatternMultiToolTabs tab) {
		this.tab = tab;
	}

	private static class ObjPatternMultiToolInventoryFilter implements IAEItemFilter {
		private ObjPatternMultiToolInventoryFilter() {
		}

		public boolean allowExtract(IItemHandler inv, int slot, int amount) {
			return true;
		}

		public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
			return stack.getItem() instanceof ItemEncodedPattern;
		}
	}
}


