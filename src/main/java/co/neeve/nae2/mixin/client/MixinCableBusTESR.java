package co.neeve.nae2.mixin.client;

import appeng.tile.AEBaseTile;
import appeng.tile.networking.CableBusTESR;
import appeng.tile.networking.TileCableBusTESR;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CableBusTESR.class)
public class MixinCableBusTESR extends MixinTileEntitySpecialRenderer<AEBaseTile> {
	@Override
	public boolean isGlobalRenderer(AEBaseTile te) {
		return te instanceof TileCableBusTESR realTe
			&& realTe.getCableBus() instanceof IBeamFormerHost host && host.hasBeamFormers();
	}
}
