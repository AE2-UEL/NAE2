package co.neeve.nae2.mixin.voidcell;

import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.me.storage.DriveWatcher;
import co.neeve.nae2.common.interfaces.IVoidingCellHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(value = DriveWatcher.class, remap = false)
public class MixinDriveWatcher {
	@WrapOperation(
		method = { "injectItems", "extractItems" },
		constant = @Constant(classValue = CreativeCellHandler.class)
	)
	private static boolean wrapInstanceOfCheck(Object obj, Operation<Boolean> operation) {
		return operation.call(obj) || obj instanceof IVoidingCellHandler;
	}
}
