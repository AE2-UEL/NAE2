package co.neeve.nae2.common.parts.implementations;

import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.IGridProxyable;
import co.neeve.nae2.common.helpers.Puller;
import net.minecraft.tileentity.TileEntity;

public interface IPullerHost extends IActionHost, IGridProxyable, IUpgradeableHost {
	Puller getPuller();

	void markForSave();

	TileEntity getTileEntity();
}
