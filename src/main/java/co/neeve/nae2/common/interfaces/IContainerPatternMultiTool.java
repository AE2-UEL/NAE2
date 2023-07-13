package co.neeve.nae2.common.interfaces;

import co.neeve.nae2.common.slots.SlotPatternMultiToolUpgrade;
import net.minecraft.entity.player.EntityPlayer;

public interface IContainerPatternMultiTool extends IPatternMultiToolHost {
	boolean canTakeStack(SlotPatternMultiToolUpgrade slot, EntityPlayer player);

	boolean isViewingInterface();

	void toggleInventory();
}
