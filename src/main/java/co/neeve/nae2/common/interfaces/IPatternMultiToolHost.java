package co.neeve.nae2.common.interfaces;

import co.neeve.nae2.common.items.patternmultitool.ObjPatternMultiTool;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface IPatternMultiToolHost {
	@Nullable
	ObjPatternMultiTool getPatternMultiToolObject();

	default @Nullable IItemHandler getPatternMultiToolInventory() {
		ObjPatternMultiTool pmtObject = this.getPatternMultiToolObject();
		if (pmtObject == null) return null;
		return pmtObject.getPatternInventory();
	}

	default @Nullable IItemHandler getSearchReplaceInventory() {
		ObjPatternMultiTool pmtObject = this.getPatternMultiToolObject();
		if (pmtObject == null) return null;
		return pmtObject.getSearchReplaceInventory();
	}


	default boolean isPatternMultiToolSlotEnabled(int i) {
		ObjPatternMultiTool pmtObject = this.getPatternMultiToolObject();
		if (pmtObject == null) return false;
		return i <= this.getPatternMultiToolObject().getInstalledCapacityUpgrades();
	}

}
