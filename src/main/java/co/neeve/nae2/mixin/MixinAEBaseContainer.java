package co.neeve.nae2.mixin;

import appeng.api.networking.security.IActionSource;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import co.neeve.nae2.common.containers.ContainerPatternMultiplier;
import co.neeve.nae2.common.slots.SlotPatternMultiplier;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(AEBaseContainer.class)
public class MixinAEBaseContainer extends Container {
    @Shadow
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return false;
    }

    @Shadow
    public InventoryPlayer getInventoryPlayer() {
        return null;
    }

    @Shadow
    protected @NotNull Slot addSlotToContainer(@NotNull Slot newSlot) {
        return null;
    }

    @Shadow
    public void lockPlayerInventorySlot(int idx) {
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE",
                    target = "Lappeng/container/slot/AppEngSlot;isPlayerSide()Z",
                    ordinal = 2
            )
    )
    public void injectPMTSlots(EntityPlayer p, int idx, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 1) AppEngSlot cs, @Local List<AppEngSlot> selectedSlots, @Local ItemStack tis) {
        if (!(((Object) this) instanceof ContainerPatternMultiplier) && cs instanceof SlotPatternMultiplier && cs.isItemValid(tis)) {
            selectedSlots.add(cs);
        }
    }

    @Inject(method = "transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
                    ordinal = 4
            )
    )
    public void sortPMTSlots(EntityPlayer p, int idx, CallbackInfoReturnable<ItemStack> cir, @Local List<Slot> selectedSlots) {
        selectedSlots.sort(Comparator.comparing(o -> !(o instanceof SlotPatternMultiplier)));
    }

    @Shadow
    public IActionSource getActionSource() {
        return null;
    }
}
