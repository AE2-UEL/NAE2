package co.neeve.nae2.mixin.reconchamber;

import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.api.lens.Lens;
import de.ellpeck.actuallyadditions.api.recipe.LensConversionRecipe;
import de.ellpeck.actuallyadditions.mod.misc.apiimpl.MethodHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MethodHandler.class, remap = false)
public class MixinMethodHandler {
	@WrapOperation(method = "invokeConversionLens", at = @At(
		value = "INVOKE",
		target = "Lde/ellpeck/actuallyadditions/mod/items/lens/LensRecipeHandler;findMatchingRecipe" +
			"(Lnet/minecraft/item/ItemStack;Lde/ellpeck/actuallyadditions/api/lens/Lens;)" +
			"Lde/ellpeck/actuallyadditions/api/recipe/LensConversionRecipe;",
		ordinal = 0
	))
	private LensConversionRecipe invokeConversionLens(ItemStack is, Lens lens,
	                                                  Operation<LensConversionRecipe> operation,
	                                                  @Local IAtomicReconstructor reconstructor,
	                                                  @Local(name = "state") IBlockState state,
	                                                  @Local(name = "pos") BlockPos pos) {
		if (reconstructor.getWorldObject().getTileEntity(pos) instanceof TileReconstructionChamber trc) {
			trc.handleConversionRecipe(reconstructor);
			return null;
		}
		return operation.call(is, lens);
	}
}
