package co.neeve.nae2.mixin.jei.cellview;

import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.api.ApiClientHelper;
import co.neeve.nae2.common.features.subfeatures.JEIFeatures;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.config.KeyBindings;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ApiClientHelper.class, remap = false)
public class MixinApiClientHelper {
	@Inject(method = "addCellInformation", at = @At(
		value = "RETURN"
	))
	private <T extends IAEStack<T>> void addCellInformation(ICellInventoryHandler<T> handler, List<String> lines,
	                                                        CallbackInfo ci) {
		lines.add("");
		lines.add(I18n.format("nae2.jei.cellview.keybind", KeyBindings.showRecipe.getDisplayName()));
	}

	@WrapOperation(method = "addCellInformation", at = @At(
		value = "INVOKE",
		target = "Lappeng/api/storage/ICellInventory;getAvailableItems(Lappeng/api/storage/data/IItemList;)" +
			"Lappeng/api/storage/data/IItemList;"
	))
	private <T extends IAEStack<T>> IItemList<T> getAvailableItems(ICellInventory<T> instance, IItemList<T> in,
	                                                               Operation<IItemList<T>> operation,
	                                                               @Local ICellInventoryHandler<T> handler) {
		// Return the same list without filling it.
		if (!handler.isPreformatted() && JEIFeatures.CELL_VIEW.isEnabled()) return in;

		return operation.call(instance, in);
	}

}
