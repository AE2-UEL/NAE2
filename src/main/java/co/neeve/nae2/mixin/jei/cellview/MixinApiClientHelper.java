package co.neeve.nae2.mixin.jei.cellview;

import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.core.api.ApiClientHelper;
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
}
