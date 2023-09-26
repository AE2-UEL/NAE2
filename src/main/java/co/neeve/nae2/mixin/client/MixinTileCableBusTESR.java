package co.neeve.nae2.mixin.client;

import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileCableBusTESR;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TileCableBusTESR.class)
public abstract class MixinTileCableBusTESR extends TileCableBus {
	@Unique
	private boolean nae2$oldHasFormers = false;

	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		if (this.getCableBus() instanceof IBeamFormerHost host) {
			var hasFormers = host.hasBeamFormers();
			if (!nae2$oldHasFormers && hasFormers) {
				var tile = getTile();
				BlockPos pos = tile.getPos();
				var x = pos.getX();
				var y = pos.getY();
				var z = pos.getZ();
				Minecraft.getMinecraft().renderGlobal
					.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
			}

			nae2$oldHasFormers = hasFormers;

			return INFINITE_EXTENT_AABB;
		}

		return super.getRenderBoundingBox();
	}
}
