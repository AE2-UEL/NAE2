package co.neeve.nae2.mixin.dualityinterface;

import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import co.neeve.nae2.common.helpers.DualityInterfaceHelper;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * PushPattern shims.
 * <p>
 * Our primary goal here is to tell AE2 that it can push into tunnels, while preserving the default behavior.
 * <p>
 * We don't actually push items here yet. Just let AE2 know, which makes it all easier.
 */
@Mixin(value = DualityInterface.class, remap = false)
public class MixinPushPattern {
	@Unique
	public HashMap<Object, EnumFacing> nae2$visitedFaces = new HashMap<>();
	@Shadow
	@Final
	private IInterfaceHost iHost;

	/**
	 * Won't be using this, so doesn't matter. Just avoid the block.
	 */
	@Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Ljava/util/EnumSet;isEmpty()Z"))
	public boolean shimPushPatternEmptyCheck(EnumSet<EnumFacing> instance) {
		return false;
	}

	/**
	 * Literally doesn't matter what we do here. Return something so JVM doesn't complain.
	 * Don't let this progress the iterator. We will progress it ourselves later on.
	 */
	@Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()" +
		"Ljava/lang/Object;"))
	public Object shimPushPatternIterator(Iterator<EnumSet<EnumFacing>> ignored) {
		return EnumFacing.UP;
	}

	/**
	 * The gathering.
	 */
	@Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Ljava/util/EnumSet;iterator()" +
		"Ljava/util/Iterator;", ordinal = 0))
	public Iterator<Map.Entry<Object, EnumFacing>> injectPushPatternTEGathering(EnumSet<EnumFacing> instance) {
		if (nae2$visitedFaces.isEmpty())
			this.nae2$visitedFaces = DualityInterfaceHelper.getTileEntitiesAroundInterface(this.iHost);

		return nae2$visitedFaces.entrySet().iterator();
	}

	/**
	 * Replace all references to "s". This is the only EnumFacing variable, so...
	 */
	@ModifyVariable(method = "pushPattern", at = @At("LOAD"))
	public EnumFacing replacePushPatternEnumFacing(EnumFacing original, @Share("side") LocalRef<EnumFacing> side) {
		return side.get();
	}

	/**
	 * Irrelevant. Shim so this doesn't raise NPE.
	 */
	@Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;offset" +
		"(Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;", ordinal = 0))
	public BlockPos shimPushPatternOffset(BlockPos instance, EnumFacing facing) {
		return instance;
	}

	@Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Ljava/util/EnumSet;remove" +
		"(Ljava/lang/Object;)Z"))
	public boolean detourPushPatternRemoval(EnumSet<EnumFacing> instance, Object ignored,
	                                        @Local Iterator<Map.Entry<TileEntity, EnumFacing>> iterator) {
		iterator.remove();
		return true;
	}

	/**
	 * We already know what TEs surround the interface.
	 * Detour the getTileEntity method and discard all parameters.
	 * Progress the iterator instead.
	 */
	@Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity" +
		"(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", ordinal = 0))
	public TileEntity detourPushPatternGetTileEntity(World instance, BlockPos ignored, @Local Iterator<Map.Entry<Object
		, EnumFacing>> iterator, @Share("side") LocalRef<EnumFacing> side,
	                                                 @Share("tunnel") LocalRef<PartP2PInterface> tunnel) {

		var entry = (iterator).next();
		side.set(entry.getValue());
		var obj = entry.getKey();
		if (obj instanceof TileEntity te) {
			tunnel.set(null);
			return te;
		} else if (obj instanceof PartP2PInterface p2pi) {
			// Suddenly full. Huh?
			if (p2pi.hasItemsToSend()) {
				tunnel.set(null);
				return null;
			}

			var te = p2pi.getFacingTileEntity();
			tunnel.set(p2pi);
			return te;
		} else {
			throw new IllegalStateException();
		}
	}

	@Inject(method = "pushPattern", at = @At(value = "INVOKE", target = "Lappeng/helpers/DualityInterface;" +
		"acceptsItems" +
		"(Lappeng/util/InventoryAdaptor;Lnet/minecraft/inventory/InventoryCrafting;)Z", shift = At.Shift.AFTER),
		cancellable = true)
	public void injectPushPatternTunnelFilling(CallbackInfoReturnable<Boolean> cir,
	                                           @Share("tunnel") LocalRef<PartP2PInterface> tunnel,
	                                           @Local InventoryCrafting table) {
		// it was dio all along
		PartP2PInterface partP2PInterface = tunnel.get();
		if (partP2PInterface != null) {
			for (int x = 0; x < table.getSizeInventory(); ++x) {
				ItemStack is = table.getStackInSlot(x);
				if (!is.isEmpty()) {
					partP2PInterface.addToSendList(is);
				}
			}

			partP2PInterface.pushItemsOut();

			cir.setReturnValue(true);
		}
	}
}
