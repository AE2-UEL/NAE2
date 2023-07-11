package co.neeve.nae2.client.gui.interfaces;

import co.neeve.nae2.common.interfaces.IPatternMultiplierHost;
import co.neeve.nae2.items.patternmultiplier.ObjPatternMultiplier;
import net.minecraft.client.gui.inventory.GuiContainer;

public interface IPatternMultiplierHostGui {
    default ObjPatternMultiplier getPMTObject() {
        if (this instanceof GuiContainer gc && gc.inventorySlots instanceof IPatternMultiplierHost pmh) {
            return pmh.getPMTObject();
        }

        return null;
    }

    int getPMTOffsetX();

    int getPMTOffsetY();

    int getGuiTop();

    int getGuiLeft();
}
