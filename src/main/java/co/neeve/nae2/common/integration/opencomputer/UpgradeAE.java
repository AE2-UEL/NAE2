package co.neeve.nae2.common.integration.opencomputer;

import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.tile.misc.TileSecurityStation;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Visibility;

public class UpgradeAE extends NetworkControl<TileSecurityStation> {
    private final EnvironmentHost host;

    public UpgradeAE(EnvironmentHost envHost) {
        this.host = envHost;
        setNode(Network.newNode(this, Visibility.Network)
                .withConnector()
                .withComponent("upgrade_me", Visibility.Network)
                .create());
    }

    @Override
    public TileSecurityStation tile() throws SecurityException {
        TileSecurityStation security = (TileSecurityStation) getSecurity();
        if (security == null) {
            throw new SecurityException("No Security Station");
        }

        IGridNode node = security.getGridNode(AEPartLocation.INTERNAL);
        if (node == null) {
            throw new SecurityException("No Security Station");
        }

        IGridBlock gridBlock = node.getGridBlock();
        if (gridBlock == null) {
            throw new SecurityException("No Security Station");
        }

        DimensionalCoord location = gridBlock.getLocation();
        if (location == null) {
            throw new SecurityException("No Security Station");
        }

        TileSecurityStation tileSecurity = (TileSecurityStation) location.getWorld().getTileEntity(location.getPos());
        if (tileSecurity == null) {
            throw new SecurityException("No Security Station");
        }

        return tileSecurity;
    }

    @Override
    public EnvironmentHost host() {
        return host;
    }
}
