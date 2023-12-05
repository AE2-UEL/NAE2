package co.neeve.nae2.mixin.ifacep2p.shared;

import appeng.api.implementations.items.IMemoryCard;
import appeng.core.Api;
import appeng.parts.p2p.PartP2PTunnel;
import co.neeve.nae2.NAE2;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PartP2PTunnel.class)
public class MixinPartP2PTunnel {
	@ModifyVariable(method = "onPartActivate", at = @At(value = "LOAD"), name = "newType", remap = false)
	public ItemStack injectP2PTypes(ItemStack newType, @Local(ordinal = 0) ItemStack hand) {
		var item = hand.getItem();

		if (!(item instanceof IMemoryCard)) {
			var definitions = Api.INSTANCE.definitions();

			var iface = definitions.blocks().iface().maybeStack(1);
			var ifacePart = definitions.parts().iface().maybeStack(1);

			if ((iface.isPresent() && item.equals(iface.get().getItem()))
				|| (ifacePart.isPresent() && item.equals(ifacePart.get().getItem()))) {
				return NAE2.definitions().parts().p2pTunnelInterface().maybeStack(1).orElse(ItemStack.EMPTY);
			}
		}

		return newType;
	}
}
