package co.neeve.nae2.mixin.dualityinterface;

import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import co.neeve.nae2.common.helpers.DualityInterfaceHelper;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;

/**
 * isBusy shims.
 * <p>
 * Main goal is replacing the iterator. Instead of looping over six sides by default,
 * we gather all possible TEs around while also looking into tunnels for extra TEs and loop over TEs instead.
 * <p>
 * Gross, but should be perfectly compatible with the current PAE2 logic.
 */
@SuppressWarnings("rawtypes")
@Mixin(value = DualityInterface.class, remap = false)
public class MixinBlocking {
	@Shadow
	@Final
	private IInterfaceHost iHost;

	/**
	 * Literally doesn't matter what we do here. Return something so JVM doesn't complain.
	 * Don't let this progress the iterator. We will progress it ourselves later on.
	 */
	@Redirect(method = "isBusy", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
	public Object shimBusyIterator(Iterator ignored) {
		return EnumFacing.UP;
	}

	/**
	 * We already know what TEs surround the interface.
	 * Detour the getTileEntity method and discard all parameters.
	 * Progress the iterator instead.
	 */
	@Redirect(method = "isBusy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity" +
		"(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", ordinal = 0))
	public TileEntity detourIsBusyGetTileEntity(World instance, BlockPos ignored, @Local Iterator<Map.Entry<Object
		, EnumFacing>> iterator, @Share("side") LocalRef<EnumFacing> side) {

		var entry = (iterator).next();
		side.set(entry.getValue());
		var obj = entry.getKey();
		if (obj instanceof TileEntity te) {
			return te;
		} else if (obj instanceof PartP2PInterface p2pi) {
			return p2pi.getFacingTileEntity();
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * Replace all references to "s". This is the only EnumFacing variable, so...
	 */
	@ModifyVariable(method = "isBusy", at = @At("LOAD"))
	public EnumFacing replaceIsBusyEnumFacing(EnumFacing original, @Share("side") LocalRef<EnumFacing> side) {
		return side.get();
	}

	/**
	 * Irrelevant. Shim so this doesn't raise NPE.
	 */
	@Redirect(method = "isBusy", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;offset" +
		"(Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;", ordinal = 0))
	public BlockPos shimIsBusyOffset(BlockPos instance, EnumFacing facing) {
		return instance;
	}

	/**
	 * This is where the fun begins.
	 * Loop over all provided directions and gather all TEs we see.
	 * We don't care about WHAT TEs we see, just so that it's a TE.
	 * AE2 will check everything for us.
	 */
	@Redirect(method = "isBusy", at = @At(value = "INVOKE", target = "Ljava/util/EnumSet;iterator()" +
		"Ljava/util/Iterator;"))
	public Iterator<Map.Entry<Object, EnumFacing>> replaceBusyIterator(EnumSet<EnumFacing> instance) {
		return DualityInterfaceHelper.getTileEntitiesAroundInterface(this.iHost).entrySet().iterator();
	}
}
