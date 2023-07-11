package co.neeve.nae2.common.interfaces;

import appeng.container.slot.AppEngSlot;
import appeng.parts.automation.UpgradeInventory;
import co.neeve.nae2.common.enums.PatternMultiplierInventories;
import co.neeve.nae2.items.patternmultiplier.ObjPatternMultiplier;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public interface IPatternMultiplierHost {
    ObjPatternMultiplier getPMTObject();

    List<AppEngSlot> getPatternMultiplierSlots();

    default IItemHandler getPatternInventory() {
        return this.getPMTObject().getPatternInventory();
    }

    default UpgradeInventory getUpgradeInventory() {
        return this.getPMTObject().getUpgradeInventory();
    }

    default boolean isPatternSlotEnabled(int i) {
        // ArchengiusT reference
        PatternMultiplierInventories which = this instanceof IContainerPatternMultiplier cmp ?
                (cmp.isViewingInterface() ?
                        PatternMultiplierInventories.INTERFACE
                        : PatternMultiplierInventories.PMT)
                : PatternMultiplierInventories.PMT;

        return i <= this.getPMTObject().getInstalledCapacityUpgrades();
    }
}
