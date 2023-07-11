package co.neeve.nae2.mixin.client;

import appeng.client.gui.implementations.GuiInterfaceTerminal;
import appeng.container.slot.AppEngSlot;
import co.neeve.nae2.client.gui.PatternMultiplierGUIHelper;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiplierHostGui;
import co.neeve.nae2.common.slots.SlotPatternMultiplier;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.List;

@Mixin(GuiInterfaceTerminal.class)
public class MixinGuiInterfaceTerminal extends MixinAEBaseGui implements IPatternMultiplierHostGui {
    public MixinGuiInterfaceTerminal(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(method = "Lappeng/client/gui/implementations/GuiInterfaceTerminal;drawBG(IIII)V", at = @At("HEAD"), remap = false)
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.getPMTObject() != null) {
            PatternMultiplierGUIHelper.drawPMTGui(this, offsetX, offsetY);
        }
    }

    @Inject(method = "Lappeng/client/gui/implementations/GuiInterfaceTerminal;repositionSlot(Lappeng/container/slot/AppEngSlot;)V",
            at = @At(value = "HEAD"),
            remap = false,
            cancellable = true
    )
    public void repositionSlots(final AppEngSlot s, CallbackInfo ci) {
        if (s instanceof SlotPatternMultiplier) {
            s.yPos = s.getY() + 11;
            ci.cancel();
        }
    }

    @Inject(method = "getJEIExclusionArea", at = @At("RETURN"), remap = false)
    public void injectJEIAreas(CallbackInfoReturnable<List<Rectangle>> cir) {
        cir.getReturnValue().addAll(super.getJEIExclusionArea());
    }

    @Override
    public int getPMTOffsetX() {
        return -63 - 18 - 7 - 1;
    }

    @Override
    public int getPMTOffsetY() {
        return 43 + 16 - 7 - 1 + 10;
    }
}
