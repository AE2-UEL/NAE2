package co.neeve.nae2.mixin.ifacep2p.shared;

import appeng.api.implementations.IUpgradeableHost;
import appeng.api.parts.IPartHost;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import co.neeve.nae2.common.integration.ae2fc.AE2FCInterfaceHelper;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomface;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * isBusy stuff.
 * Instead of looping over just the six sides, we check if the entity being looked at is a tunnel.
 * If it is, we hijack the entire thing.
 * This virtually doesn't affect the default behavior.
 */
@SuppressWarnings("rawtypes")
@Mixin(value = DualityInterface.class, remap = false)
public abstract class MixinBlockingTermName {
	@Unique
	private EnumFacing nae2$originalFacing;

	@Shadow
	public abstract TileEntity getTile();

	@Shadow
	public abstract IUpgradeableHost getHost();

	@WrapOperation(
		method = { "isBusy", "getTermName" },
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)" +
				"Lnet/minecraft/tileentity/TileEntity;",
			ordinal = 0,
			remap = true
		)
	)
	private TileEntity wrapEntityGetter(World instance, BlockPos bp, Operation<TileEntity> operation,
	                                    @Local LocalRef<EnumFacing> facingRef, @Local Iterator iterator,
	                                    @Share("tunnelTiles") LocalRef<LinkedList<Pair<EnumFacing, TileEntity>>> tunnelTEs) {
		var tiles = tunnelTEs.get();

		// Safeguard, though this has absolutely no chance of happening naturally.
		if (tiles != null && tiles.isEmpty()) {
			tiles = null;
			tunnelTEs.set(null);
		}

		// There's a pending tunnel to be iterated. Iterate it instead.
		if (tiles != null) {
			// Pop one entity and feed it instead, supplying the output tunnel's facing value.
			var pair = tiles.removeFirst();
			if (tiles.isEmpty())
				tunnelTEs.set(null);

			facingRef.set(pair.getLeft());

			if (Platform.isModLoaded("ae2fc")) {
				final IInterfaceHost interfaceHost;
				if (this.getHost() instanceof IInterfaceHost iInterfaceHost) {
					interfaceHost = iInterfaceHost;
				} else if (this.getTile() instanceof IInterfaceHost iInterfaceHost) {
					interfaceHost = iInterfaceHost;
				} else {
					interfaceHost = null;
				}

				AE2FCInterfaceHelper.setEnumFacingOverride(this.nae2$originalFacing.getOpposite());
				AE2FCInterfaceHelper.setInterfaceOverride(
					interfaceHost != null ? interfaceHost.getTileEntity() : null);
			}
			return pair.getRight();
		}

		// Fetch entity using the original method. Get current facing.
		var te = operation.call(instance, bp);
		var facing = facingRef.get();

		// Is the entity an input tunnel?
		if (te instanceof IPartHost ph && ph.getPart(facing.getOpposite()) instanceof PartP2PInterface tunnel && !tunnel.isOutput()) {
			var outputs = Lists.newLinkedList(tunnel.getCachedOutputsRecursive());
			if (!outputs.isEmpty()) {
				var outputTiles = new LinkedList<Pair<EnumFacing, TileEntity>>();
				for (var output : outputs) {
					if (output.hasItemsToSend()) continue;

					var outputTile = output.getFacingTileEntity();
					if (outputTile == null)
						continue;

					outputTiles.add(Pair.of(output.getSide().getFacing(), outputTile));
				}

				this.nae2$originalFacing = tunnel.getSide().getFacing().getOpposite();
				tunnelTEs.set(outputTiles);
			}

			return null; // Skip. :)
		}

		if (Platform.isModLoaded("ae2fc")) {
			AE2FCInterfaceHelper.setEnumFacingOverride(null);
			AE2FCInterfaceHelper.setInterfaceOverride(null);
		}
		return te;
	}

	@ModifyExpressionValue(method = { "isBusy", "getTermName" }, at = @At(
		value = "INVOKE",
		target = "Ljava/util/Iterator;hasNext()Z"
	))
	private boolean wrapIteratorHasNext(boolean original,
	                                    @Share("tunnelTiles") LocalRef<LinkedList<Pair<EnumFacing, TileEntity>>> tunnelTEs) {
		// Continue iterating even if there's no next value, if we're supplying tunnel ents.
		return original || tunnelTEs.get() != null;
	}

	@WrapOperation(method = { "isBusy", "getTermName" }, at = @At(
		value = "INVOKE",
		target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
	))
	private Object wrapIteratorNext(Iterator iterator, Operation<Object> operation,
	                                @Share("tunnelTiles") LocalRef<LinkedList<Pair<EnumFacing, TileEntity>>> tunnelTEs) {
		// Check if we're iterating tunnels. If we are, the value returned doesn't matter, since we supply our own
		// in the next method. Return something bogus to keep JVM happy.
		if (tunnelTEs.get() != null) {
			return EnumFacing.UP;
		} else {
			return operation.call(iterator);
		}
	}

	@WrapOperation(method = { "isBusy", "pushPattern" }, at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)" +
			"Lnet/minecraft/block/state/IBlockState;",
		remap = true
	))
	private IBlockState wrapBlockStateFixUp(World instance, BlockPos blockPos, Operation<IBlockState> operation,
	                                        @Local(name = "te") TileEntity te) {
		if (Platform.isModLoaded("actuallyadditions") && te instanceof TileEntityPhantomface)
			return operation.call(instance, blockPos);

		return instance.getBlockState(te.getPos());
	}

	@WrapOperation(method = { "getTermName" }, at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)" +
			"Lnet/minecraft/block/state/IBlockState;",
		remap = true
	))
	private IBlockState wrapBlockStateFixUpTermName(World instance, BlockPos blockPos,
	                                                Operation<IBlockState> operation,
	                                                @Local(name = "directedTile") TileEntity te) {
		// This is because the TileEntity variable is different. :P
		if (Platform.isModLoaded("actuallyadditions") && te instanceof TileEntityPhantomface)
			return operation.call(instance, blockPos);

		return instance.getBlockState(te.getPos());
	}
}
