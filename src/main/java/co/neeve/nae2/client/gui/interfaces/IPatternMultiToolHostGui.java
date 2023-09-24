package co.neeve.nae2.client.gui.interfaces;

import co.neeve.nae2.common.interfaces.IPatternMultiToolHost;
import co.neeve.nae2.common.items.patternmultitool.ObjPatternMultiTool;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;

public interface IPatternMultiToolHostGui {
	default @Nullable ObjPatternMultiTool getPMTObject() {
		if (this instanceof GuiContainer gc && gc.inventorySlots instanceof IPatternMultiToolHost pmh) {
			return pmh.getPatternMultiToolObject();
		}

		return null;
	}

	int getPMTOffsetX();

	int getPMTOffsetY();

	int getGuiTop();

	int getGuiLeft();


}
