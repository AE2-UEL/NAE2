package co.neeve.nae2.mixin;

import appeng.api.implementations.items.IMemoryCard;
import appeng.parts.p2p.PartP2PTunnel;
import co.neeve.nae2.common.registries.Parts;
import co.neeve.nae2.items.parts.NAEBaseItemPart;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PartP2PTunnel.class)
public class MixinPartP2PTunnel {
	@ModifyVariable(method = "onPartActivate", at = @At(value = "LOAD"), ordinal = 1, remap = false)
	public ItemStack injectP2PTypes(ItemStack newType, @Local(ordinal = 0) ItemStack hand) {
		if (hand.getItem() instanceof IMemoryCard) return newType;

		return new ItemStack(NAEBaseItemPart.instance, 1, Parts.P2P_TUNNEL_INTERFACE.ordinal());
	}
}
