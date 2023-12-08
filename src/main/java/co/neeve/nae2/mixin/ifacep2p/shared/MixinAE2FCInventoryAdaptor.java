package co.neeve.nae2.mixin.ifacep2p.shared;

import appeng.helpers.IInterfaceHost;
import co.neeve.nae2.common.integration.ae2fc.AE2FCIntegrationHelper;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidConvertingInventoryAdaptor.class, remap = false)
public class MixinAE2FCInventoryAdaptor {
	@WrapOperation(method = "wrap", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)" +
			"Lnet/minecraft/tileentity/TileEntity;",
		remap = true
	))
	private static TileEntity wrapTileEntity(World instance, BlockPos blockPos, Operation<TileEntity> original) {
		var override = AE2FCIntegrationHelper.getInterfaceOverride();
		if (override != null) {
			return override;
		}

		return original.call(instance, blockPos);
	}

	@WrapOperation(method = "wrap", at = @At(
		value = "INVOKE",
		target = "Lcom/glodblock/github/inventory/FluidConvertingInventoryAdaptor;getInterfaceTE" +
			"(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)Lappeng/helpers/IInterfaceHost;"
	))
	private static IInterfaceHost wrapGetInterfaceTE(TileEntity te, EnumFacing facing,
	                                                 Operation<IInterfaceHost> operation) {
		var override = AE2FCIntegrationHelper.getInterfaceOverride();
		if (override != null) {
			return operation.call(override, AE2FCIntegrationHelper.getEnumFacingOverride());
		}

		return operation.call(te, facing);
	}
}
