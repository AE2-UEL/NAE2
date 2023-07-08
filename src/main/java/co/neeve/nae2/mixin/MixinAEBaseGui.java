package co.neeve.nae2.mixin;

import appeng.client.gui.AEBaseGui;
import appeng.client.render.StackSizeRenderer;
import appeng.container.slot.AppEngSlot;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.core.ext.IExtendedAEItemStack;
import co.neeve.nae2.item.patternmultiplier.client.GuiPatternMultiplier;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseGui.class)
public abstract class MixinAEBaseGui extends GuiScreen {
    @Final
    @Shadow
    private StackSizeRenderer stackSizeRenderer;

    @Inject(method = "drawSlot",
            at = @At(
                    target = "Lappeng/client/render/StackSizeRenderer;renderStackSize(Lnet/minecraft/client/gui/FontRenderer;Lappeng/api/storage/data/IAEItemStack;II)V",
                    value = "INVOKE"
            ), cancellable = true)
    public void drawSlot(Slot s, @NotNull CallbackInfo ci) {
        if (((Object) this) instanceof GuiPatternMultiplier && s instanceof AppEngSlot aes) {
            AEItemStack ais = AEItemStack.fromItemStack(aes.getDisplayStack());

            ItemStack is = aes.getStack();
            if (ais instanceof IExtendedAEItemStack eais && is.getItem() instanceof ItemEncodedPattern)
                eais.setExtendedCount(is.getCount());

            this.stackSizeRenderer.renderStackSize(fontRenderer, ais, s.xPos, s.yPos);
            ci.cancel();
        }
    }
}