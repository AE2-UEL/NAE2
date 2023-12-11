package co.neeve.nae2.mixin.patternmultitool.client;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.StackSizeRenderer;
import co.neeve.nae2.common.interfaces.IExtendedAEItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StackSizeRenderer.class)
public abstract class MixinStackSizeRenderer {
	@Inject(method = "renderStackSize", at = @At(
		value = "INVOKE",
		shift = At.Shift.BEFORE,
		target = "Lnet/minecraft/client/renderer/GlStateManager;enableLighting()V")
	)
	private void renderStackExtended(FontRenderer fontRenderer, IAEItemStack aeStack, int xPos, int yPos,
	                                 CallbackInfo ci) {
		if (aeStack instanceof IExtendedAEItemStack eais && eais.getExtendedCount() > 1) {
			var count = eais.getExtendedCount();

			var str = String.valueOf(count);
			var fr = Minecraft.getMinecraft().fontRenderer;
			fr.drawStringWithShadow(str, (float) (xPos + 19 - 2 - fr.getStringWidth(str)), yPos, 16777215);
		}
	}
}
