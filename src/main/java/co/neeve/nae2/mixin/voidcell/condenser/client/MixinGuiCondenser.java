package co.neeve.nae2.mixin.voidcell.condenser.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCondenser;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiCondenser.class, remap = false)
public abstract class MixinGuiCondenser extends AEBaseGui {
	public MixinGuiCondenser(Container container) {
		super(container);
	}

	@Inject(method = "drawBG", at = @At("RETURN"))
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY, CallbackInfo ci) {
		this.drawTexturedModalRect(offsetX + 100, offsetY + 52 + 26 - 1, 50, 51, 18, 18);
	}
}
