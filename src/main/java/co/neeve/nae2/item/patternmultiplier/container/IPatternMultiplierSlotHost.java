package co.neeve.nae2.item.patternmultiplier.container;

import appeng.container.slot.IOptionalSlotHost;
import net.minecraft.entity.player.EntityPlayer;

public interface IPatternMultiplierSlotHost extends IOptionalSlotHost {
    boolean canTakeStack(SlotPatternMultiplierUpgrade slot, EntityPlayer player);
}
