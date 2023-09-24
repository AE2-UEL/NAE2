package co.neeve.nae2.mixin.dualityinterface;

import appeng.api.parts.IPartHost;
import appeng.helpers.DualityInterface;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * isBusy stuff.
 * Instead of looping over just the six sides, we check if the entity being looked at is a tunnel.
 * If it is, we hijack the entire thing.
 * This virtually doesn't affect the default behavior.
 */
@SuppressWarnings("rawtypes")
@Mixin(value = DualityInterface.class, remap = false)
public class MixinBlocking {
	@WrapOperation(
		method = "isBusy",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)" +
				"Lnet/minecraft/tileentity/TileEntity;",
			ordinal = 0,
			remap = true
		)
	)
	private TileEntity wrapBusyGetTE(World instance, BlockPos bp, Operation<TileEntity> operation,
	                                 @Local LocalRef<EnumFacing> facingRef, @Local Iterator iterator,
	                                 @Share("tunnelTiles") LocalRef<LinkedList<Pair<EnumFacing, TileEntity>>> tunnelTEs) {
		var tiles = tunnelTEs.get();

		// There's a pending tunnel to be iterated. Iterate it instead.
		if (tiles != null) {
			// Pop one entity and feed it instead, supplying the output tunnel's facing value.
			var pair = tiles.removeFirst();
			if (tiles.isEmpty())
				tunnelTEs.set(null);

			facingRef.set(pair.getLeft());
			return pair.getRight();
		}

		// Fetch entity using the original method. Get current facing.
		var te = operation.call(instance, bp);
		var facing = facingRef.get();

		// Is the entity an input tunnel?
		if (te instanceof IPartHost ph && ph.getPart(facing.getOpposite()) instanceof PartP2PInterface tunnel && !tunnel.isOutput()) {
			var outputs = tunnel.getOutputs();
			if (outputs != null) {
				var outputTiles = Streams.stream(outputs)
					.filter(x -> !x.hasItemsToSend())
					.map((output) -> {
						var outputTile = output.getFacingTileEntity();
						return outputTile == null ? null : Pair.of(output.getSide().getFacing(),
							outputTile);
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toCollection(LinkedList::new));

				// Sure it is, and we have TEs. Let the other part of this method know we're iterating them next.
				if (!outputTiles.isEmpty()) {
					tunnelTEs.set(outputTiles);
				}
			}

			return null; // Skip. :)
		}

		return te;
	}

	@ModifyExpressionValue(method = "isBusy", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Iterator;hasNext()Z"
	))
	private boolean wrapBusyHasNext(boolean original,
	                                @Share("tunnelTiles") LocalRef<LinkedList<Pair<EnumFacing, TileEntity>>> tunnelTEs) {
		// Continue iterating even if there's no next value, if we're supplying tunnel ents.
		return original || tunnelTEs.get() != null;
	}

	@WrapOperation(method = "isBusy", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
	))
	private Object wrapBusyNext(Iterator iterator, Operation<Object> operation,
	                            @Share("tunnelTiles") LocalRef<LinkedList<Pair<EnumFacing, TileEntity>>> tunnelTEs) {
		// Check if we're iterating tunnels. If we are, the value returned doesn't matter, since we supply our own
		// in the next method. Return something bogus to keep JVM happy.
		if (tunnelTEs.get() != null) {
			return EnumFacing.UP;
		} else {
			return operation.call(iterator);
		}
	}
}
