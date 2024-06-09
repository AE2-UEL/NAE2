package co.neeve.nae2.mixin.core;

import appeng.api.config.TunnelType;
import appeng.api.implementations.items.IMemoryCard;
import appeng.parts.p2p.PartP2PTunnel;
import co.neeve.nae2.NAE2;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PartP2PTunnel.class)
public class MixinTunnelConversion {
	@ModifyVariable(method = "onPartActivate", at = @At(value = "LOAD"), name = "newType", remap = false)
	public ItemStack injectP2PTypes(ItemStack newType, @Local(ordinal = 0) ItemStack hand) {
		var item = hand.getItem();

		if (!(item instanceof IMemoryCard)) {
			var conversion = NAE2.api().tunnelConversion().getConversion(hand);
			if (!conversion.isEmpty()) {
				return conversion;
			}
		}

		return newType;
	}

	@ModifyVariable(method = "onPartActivate", at = @At(value = "LOAD"), name = "tt", remap = false)
	public TunnelType maskTunnelType(TunnelType newType, @Local(ordinal = 0) ItemStack hand) {
		var item = hand.getItem();

		if (!(item instanceof IMemoryCard)) {
			var conversion = NAE2.api().tunnelConversion().getConversion(hand);
			if (!conversion.isEmpty()) {
				// Return whatever.
				return TunnelType.ME;
			}
		}

		return newType;
	}
}
