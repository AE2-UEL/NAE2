package co.neeve.nae2.common.slots;

import appeng.container.slot.SlotRestrictedInput;
import co.neeve.nae2.common.interfaces.IContainerPatternMultiTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class SlotPatternMultiToolUpgrade extends SlotRestrictedInput {
	private final IContainerPatternMultiTool host;

	public SlotPatternMultiToolUpgrade(PlacableItemType valid, IItemHandler i, IContainerPatternMultiTool host,
	                                   int slotIndex, int x, int y, InventoryPlayer p) {
		super(valid, i, slotIndex, x, y, p);
		this.host = host;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
		return this.host.canTakeStack() && super.canTakeStack(par1EntityPlayer);
	}
}
