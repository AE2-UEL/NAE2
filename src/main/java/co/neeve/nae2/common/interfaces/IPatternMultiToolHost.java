package co.neeve.nae2.common.interfaces;

import appeng.container.slot.AppEngSlot;
import appeng.parts.automation.UpgradeInventory;
import co.neeve.nae2.items.patternmultitool.ObjPatternMultiTool;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public interface IPatternMultiToolHost {
	@Nullable
	ObjPatternMultiTool getPatternMultiToolObject();

	@Nullable
	List<AppEngSlot> getPatternMultiToolSlots();

	default @Nullable IItemHandler getPatternMultiToolInventory() {
		ObjPatternMultiTool pmtObject = this.getPatternMultiToolObject();
		if (pmtObject == null) return null;
		return pmtObject.getPatternInventory();
	}

	default @Nullable UpgradeInventory getPatternMultiToolUpgradeInventory() {
		ObjPatternMultiTool pmtObject = this.getPatternMultiToolObject();
		if (pmtObject == null) return null;
		return pmtObject.getUpgradeInventory();
	}

	default boolean isPatternMultiToolSlotEnabled(int i) {
		ObjPatternMultiTool pmtObject = this.getPatternMultiToolObject();
		if (pmtObject == null) return false;
		return i <= this.getPatternMultiToolObject().getInstalledCapacityUpgrades();
	}

	@Nullable
	default ItemStack getPatternMultiToolStack() {
		ObjPatternMultiTool pmtObject = this.getPatternMultiToolObject();
		if (pmtObject == null) return null;
		return pmtObject.getItemStack();
	}
}
