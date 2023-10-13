package co.neeve.nae2.mixin.upgrades.base;

import co.neeve.nae2.common.items.NAEBaseItemUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng/items/contents/NetworkToolViewer$NetworkToolInventoryFilter", remap = false)
public class MixinNetworkTool {
	@Inject(method = "allowInsert", at = @At("HEAD"), cancellable = true)
	private void injectAllowInsert(IItemHandler inv, int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.getItem() instanceof NAEBaseItemUpgrade) {
			cir.setReturnValue(true);
		}
	}
}
