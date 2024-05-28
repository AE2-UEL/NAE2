package co.neeve.nae2.common.slots;

import appeng.container.slot.SlotDisabled;
import net.minecraftforge.items.IItemHandler;

public class SlotPatternMultiToolDisabled extends SlotDisabled implements IPMTSlot {
	public SlotPatternMultiToolDisabled(IItemHandler par1iInventory, int slotIndex, int x, int y) {
		super(par1iInventory, slotIndex, x, y);
	}

	@Override
	public void setY(int y) {
		this.yPos = y;
	}

	@Override
	public int getInitialY() {
		return this.getY();
	}
}
