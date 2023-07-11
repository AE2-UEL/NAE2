package co.neeve.nae2.mixin;

import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.slot.IOptionalSlotHost;
import appeng.helpers.IContainerCraftingPacket;
import appeng.parts.reporting.AbstractPartEncoder;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerPatternEncoder.class)
public class MixinContainerPatternEncoder extends MixinContainerMEMonitorable implements IAEAppEngInventory, IOptionalSlotHost, IContainerCraftingPacket {
    @Shadow
    public AbstractPartEncoder getPart() {
        return null;
    }

    @Shadow
    public boolean isSlotEnabled(int idx) {
        return false;
    }

    @Shadow
    public IItemHandler getInventoryByName(String string) {
        return null;
    }

    @Shadow
    public boolean useRealItems() {
        return false;
    }

    @Shadow
    public void saveChanges() {
    }

    @Shadow
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
    }
}
