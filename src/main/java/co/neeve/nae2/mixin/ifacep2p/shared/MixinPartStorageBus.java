package co.neeve.nae2.mixin.ifacep2p.shared;

import appeng.fluids.parts.PartFluidStorageBus;
import appeng.parts.misc.PartInterface;
import appeng.parts.misc.PartStorageBus;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(value = { PartStorageBus.class, PartFluidStorageBus.class }, remap = false)
public class MixinPartStorageBus {
	@WrapOperation(
		method = { "onNeighborChanged" },
		constant = @Constant(classValue = PartInterface.class)
	)
	private static boolean wrapInstanceOfCheck(Object obj, Operation<Boolean> operation) {
		return operation.call(obj) || obj instanceof PartP2PInterface;
	}
}
