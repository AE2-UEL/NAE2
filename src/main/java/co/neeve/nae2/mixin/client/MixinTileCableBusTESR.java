package co.neeve.nae2.mixin.client;

import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileCableBusTESR;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TileCableBusTESR.class)
public abstract class MixinTileCableBusTESR extends TileCableBus {
	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		if (this.getCableBus() instanceof IBeamFormerHost host && host.hasBeamFormers()) return INFINITE_EXTENT_AABB;
		return super.getRenderBoundingBox();
	}
}
