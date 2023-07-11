package co.neeve.nae2.common.interfaces;

import co.neeve.nae2.common.slots.SlotPatternMultiplierUpgrade;
import net.minecraft.entity.player.EntityPlayer;

public interface IContainerPatternMultiplier extends IPatternMultiplierHost {
    boolean canTakeStack(SlotPatternMultiplierUpgrade slot, EntityPlayer player);

    boolean isViewingInterface();

    void toggleInventory();
}
