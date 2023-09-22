package co.neeve.nae2.mixin.dualityinterface;

import appeng.api.parts.IPartHost;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * PushPattern stuff.
 * Our primary goal here is to tell AE2 that it can push into tunnels, while preserving the default behavior.
 * We will supply items into tunnels when we can.
 */
@SuppressWarnings("rawtypes")
@Mixin(value = DualityInterface.class, remap = false)
public abstract class MixinPushPattern {
	@Unique
	private PartP2PInterface nae2$inputTunnel = null;
	@Shadow
	private EnumSet<EnumFacing> visitedFaces;
	@Shadow
	@Final
	private IInterfaceHost iHost;
	@Unique
	private LinkedList<PartP2PInterface> nae2$tunnelsToVisit = null;
	@Unique
	private EnumFacing nae2$originalFacing = null;

	@ModifyExpressionValue(method = "pushPattern", at = @At(
		value = "INVOKE",
		target = "Lappeng/helpers/DualityInterface;acceptsItems(Lappeng/util/InventoryAdaptor;" +
			"Lnet/minecraft/inventory/InventoryCrafting;)Z"
	))
	private boolean rememberPushPatternAccepts(boolean accepts, @Share("accepts") LocalBooleanRef acceptsRef) {
		// goofy
		acceptsRef.set(accepts);
		return accepts;
	}

	@Inject(method = "pushPattern", at = @At(
		value = "INVOKE",
		shift = At.Shift.AFTER,
		by = 1,
		target = "Lappeng/helpers/DualityInterface;acceptsItems" +
			"(Lappeng/util/InventoryAdaptor;Lnet/minecraft/inventory/InventoryCrafting;)Z"
	), cancellable = true)
	private void injectPushPatternTunnelFilling(CallbackInfoReturnable<Boolean> cir,
	                                            @Share("tunnel") LocalRef<PartP2PInterface> currentOutputTunnelRef,
	                                            @Share("accepts") LocalBooleanRef acceptsItems,
	                                            @Local(argsOnly = true) InventoryCrafting table) {

		// it was dio all along
		var currentOutputTunnel = currentOutputTunnelRef.get();
		if (currentOutputTunnel != null) {
			if (!acceptsItems.get()) {
				cir.setReturnValue(false);
				return;
			}

			for (int x = 0; x < table.getSizeInventory(); ++x) {
				ItemStack is = table.getStackInSlot(x);
				if (!is.isEmpty()) {
					currentOutputTunnel.addToSendList(is);
				}
			}

			currentOutputTunnel.pushItemsOut();
			currentOutputTunnelRef.set(null);
			cir.setReturnValue(true);
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	@WrapOperation(
		method = "pushPattern",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)" +
				"Lnet/minecraft/tileentity/TileEntity;",
			ordinal = 0
		)
	)
	private TileEntity wrapPushGetTE(World instance, BlockPos bp, Operation<TileEntity> operation,
	                                 @Local LocalRef<EnumFacing> facingRef, @Local Iterator iterator,
	                                 @Share("tunnel") LocalRef<PartP2PInterface> currentOutputTunnel,
	                                 @Local World world) {
		// There's a pending inputTunnel to be iterated. Iterate it instead.
		if (nae2$tunnelsToVisit != null) {
			// Are we still the same?
			var te = world.getTileEntity(nae2$inputTunnel.getHost().getTile().getPos());
			if (!(te instanceof IPartHost ph && ph.getPart(nae2$originalFacing.getOpposite()) == nae2$inputTunnel)) {
				nae2$tunnelsToVisit = null;
				visitedFaces.remove(nae2$originalFacing);
				return null;
			}

			// Pop one inputTunnel and feed it instead, supplying the output inputTunnel's facing value.
			// If the list is empty, dereference it to restore AE2 behavior.
			PartP2PInterface tunnel = nae2$tunnelsToVisit.removeFirst();
			var input = nae2$inputTunnel;
			if (nae2$tunnelsToVisit.isEmpty()) {
				visitedFaces.remove(nae2$originalFacing);
				nae2$tunnelsToVisit = null;
				nae2$inputTunnel = null;
				nae2$originalFacing = null;
			}

			// Do we still belong?
			if (!input.isValidDestination(tunnel))
				return null;

			// Are we busy?
			if (tunnel.hasItemsToSend())
				return null;

			facingRef.set(tunnel.getSide().getFacing());
			currentOutputTunnel.set(tunnel);
			return tunnel.getFacingTileEntity();
		}

		// Fetch entity using the original method. Get current facing.
		var te = operation.call(instance, bp);
		var facing = facingRef.get();

		// Is the entity an input tunnel?
		if (te instanceof IPartHost ph && ph.getPart(facing.getOpposite()) instanceof PartP2PInterface inputTunnel && !inputTunnel.isOutput()) {
			var outputs = inputTunnel.getOutputs();
			if (outputs != null) {
				var outputTunnels = Streams.stream(outputs)
					.collect(Collectors.toCollection(LinkedList::new));

				// Sure it is, and we have TEs. Let the other part of this method know we're iterating them next.
				if (!outputTunnels.isEmpty()) {
					nae2$tunnelsToVisit = outputTunnels;
					nae2$originalFacing = facing;
					nae2$inputTunnel = inputTunnel;
				}
			}

			return null; // Skip. :)
		}

		currentOutputTunnel.set(null);
		nae2$tunnelsToVisit = null;
		nae2$inputTunnel = null;
		nae2$originalFacing = null;
		return te;
	}

	@WrapOperation(method = "pushPattern", at = @At(
		value = "INVOKE",
		target = "Ljava/util/EnumSet;remove(Ljava/lang/Object;)Z"
	))
	private boolean wrapPushRemove(EnumSet<EnumFacing> instance, Object obj, Operation<Boolean> operation,
	                               @Local Iterator iterator) {
		if (nae2$tunnelsToVisit == null) {
			return operation.call(instance, obj);
		}

		// Do not remove yet! We're iterating tunnels.
		return false;
	}

	@ModifyExpressionValue(method = "pushPattern", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Iterator;hasNext()Z"
	))
	private boolean wrapPushHasNext(boolean original) {
		// Continue iterating even if there's no next value, if we're supplying tunnel ents.
		return original || nae2$tunnelsToVisit != null;
	}

	@WrapOperation(method = "pushPattern", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
	))
	private Object wrapPushNext(Iterator iterator, Operation<Object> operation) {
		// Check if we're iterating tunnels. If we are, the value returned doesn't matter, since we supply our own
		// in the next method. Return something bogus to keep JVM happy.
		if (nae2$tunnelsToVisit != null) {
			return EnumFacing.UP;
		} else {
			return operation.call(iterator);
		}
	}
}
