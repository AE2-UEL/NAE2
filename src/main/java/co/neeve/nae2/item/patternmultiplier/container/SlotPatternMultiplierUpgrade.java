package co.neeve.nae2.item.patternmultiplier.container;

import appeng.container.slot.SlotRestrictedInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class SlotPatternMultiplierUpgrade extends SlotRestrictedInput {
    private final IPatternMultiplierSlotHost host;

    public SlotPatternMultiplierUpgrade(PlacableItemType valid, IItemHandler i, IPatternMultiplierSlotHost host, int slotIndex, int x, int y, InventoryPlayer p) {
        super(valid, i, slotIndex, x, y, p);
        this.host = host;
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
        Boolean hostCanTakeStack = this.host.canTakeStack(this, par1EntityPlayer);
        return hostCanTakeStack && super.canTakeStack(par1EntityPlayer);
    }
}
