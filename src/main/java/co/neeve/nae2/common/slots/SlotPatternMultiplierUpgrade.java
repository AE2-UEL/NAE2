package co.neeve.nae2.common.slots;

import appeng.container.slot.SlotRestrictedInput;
import co.neeve.nae2.common.interfaces.IContainerPatternMultiplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class SlotPatternMultiplierUpgrade extends SlotRestrictedInput {
    private final IContainerPatternMultiplier host;

    public SlotPatternMultiplierUpgrade(PlacableItemType valid, IItemHandler i, IContainerPatternMultiplier host, int slotIndex, int x, int y, InventoryPlayer p) {
        super(valid, i, slotIndex, x, y, p);
        this.host = host;
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
        return this.host.canTakeStack(this, par1EntityPlayer) &&
                super.canTakeStack(par1EntityPlayer);
    }
}
