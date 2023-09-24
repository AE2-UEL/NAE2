package co.neeve.nae2.common.helpers;

import appeng.api.parts.IPartHost;
import appeng.helpers.IInterfaceHost;
import appeng.me.cache.helpers.TunnelCollection;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DualityInterfaceHelper {
	@NotNull
	public static HashMap<Object, EnumFacing> getTileEntitiesAroundInterface(IInterfaceHost interfaceHost) {
		var iface = interfaceHost.getTileEntity();
		var sides = interfaceHost.getTargets();
		var world = iface.getWorld();
		var set = new HashMap<Object, EnumFacing>();

		for (var facing : sides) {
			var te = world.getTileEntity(iface.getPos().offset(facing));
			if (te == null) continue; // :(

			if (te instanceof IPartHost ph && ph.getPart(facing.getOpposite()) instanceof PartP2PInterface p2pi && !p2pi.isOutput()) {
				TunnelCollection<PartP2PInterface> tc = p2pi.getOutputs();

				// Instead of throwing GridAccessException, we'll just silently discard the entire tunnel.
				// I mean, even AE2 doesn't seem to handle this exception very well, why should we?
				if (tc == null) continue;

				for (var output : tc) {
					// Skip over busy.
					if (output.hasItemsToSend()) continue;

					set.put(output, facing);
				}
			} else {
				set.put(te, facing);
			}
		}
		return set;
	}
}
