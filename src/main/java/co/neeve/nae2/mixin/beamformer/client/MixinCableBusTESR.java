package co.neeve.nae2.mixin.beamformer.client;

import appeng.tile.AEBaseTile;
import appeng.tile.networking.CableBusTESR;
import appeng.tile.networking.TileCableBusTESR;
import co.neeve.nae2.common.interfaces.IBeamFormerHost;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CableBusTESR.class)
public class MixinCableBusTESR extends TileEntitySpecialRenderer<AEBaseTile> {
	@Override
	public boolean isGlobalRenderer(@NotNull AEBaseTile te) {
		return te instanceof TileCableBusTESR realTe
			&& realTe.getCableBus() instanceof IBeamFormerHost host && host.hasBeamFormers();
	}
}
