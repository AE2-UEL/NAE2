package co.neeve.nae2.mixin.patternmultitool.shared;

import appeng.api.AEApi;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.slot.SlotRestrictedInput;
import co.neeve.nae2.common.helpers.ItemHandlerHelper;
import co.neeve.nae2.common.interfaces.IPatternMultiToolHost;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("SameReturnValue")
@Mixin(ContainerPatternEncoder.class)
public class MixinContainerPatternEncoder extends MixinContainerMEMonitorable {
	/**
	 * Injects into the Pattern Encoder to try and substitute empty blank patterns.
	 */
	@WrapOperation(method = "encode", at = @At(value = "INVOKE", target = "Lappeng/container/slot" +
		"/SlotRestrictedInput;" +
		"getStack()Lnet/minecraft/item/ItemStack;", ordinal = 1))
	public ItemStack injectBlanks(SlotRestrictedInput instance, Operation<ItemStack> original) {
		var pattern = original.call(instance);
		if (pattern.isEmpty() && this instanceof IPatternMultiToolHost pmh) {
			// Try search for blanks in our inventory.
			final var pmhInv = pmh.getPatternInventory();
			final var pmhObj = pmh.getPatternMultiToolObject();
			if (pmhInv == null || pmhObj == null) return pattern;

			final var definitions = AEApi.instance().definitions();

			for (var i = 0; i < pmhInv.getSlots(); i++) {
				var is = pmhInv.getStackInSlot(i);
				if (!is.isEmpty() && definitions.materials().blankPattern().isSameAs(is)) {
					var newPattern = is.copy();
					newPattern.setCount(1);
					is.shrink(1);

					pmhObj.saveChanges();
					return newPattern;
				}
			}
		}

		return pattern;
	}

	/**
	 * Injects into the Pattern Encoder to ensure that newly created patterns are first inserted into the PMT.
	 */
	@Redirect(method = "encodeAndMoveToInventory", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/entity/player" + "/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z"))
	public boolean injectPMTOutput(InventoryPlayer ip, ItemStack itemStackIn) {
		if (this instanceof IPatternMultiToolHost pmh) {
			final var pmhInv = pmh.getPatternInventory();
			if (pmhInv != null) {
				itemStackIn = ItemHandlerHelper.insertIntoHandler(pmhInv, itemStackIn);
				if (itemStackIn.isEmpty()) {
					return true;
				}
			}
		}

		return ip.add(-1, itemStackIn);
	}
}
