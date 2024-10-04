package co.neeve.nae2.common.integration.opencomputer;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import co.neeve.nae2.NAE2;
import li.cil.oc.api.internal.Agent;
import li.cil.oc.api.internal.Drone;
import li.cil.oc.api.internal.Robot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.Iterator;

public abstract class NetworkControl<T extends TileEntity & IActionHost & IGridHost> extends AbstractManagedEnvironment {
    abstract T tile();

    abstract EnvironmentHost host();

//    private Robot robot;
//    private Drone drone;
//    private Agent agent;
    private boolean isActive = false;

    public Robot getRobot() {
        if (host() instanceof Robot) {
            return (Robot) host();
        }
        return null;
    }

    public Drone getDrone() {
        if (host() instanceof Drone) {
            return (Drone) host();
        }
        return null;
    }

    public Agent getAgent() {
        if (host() instanceof Agent) {
            return (Agent) host();
        }
        return null;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ItemStack getComponent() {
        Robot robot = getRobot();
        Drone drone = getDrone();
        if (robot != null) {
            return robot.getStackInSlot(robot.componentSlot(node().address()));
        } else if (drone != null) {
            Iterator<ItemStack> iterator = drone.internalComponents().iterator();

            while (iterator.hasNext()) {
                ItemStack item = iterator.next();
                if (item != null && item.getItem() == NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null)) {
                    return item;
                }
            }
        }

        return null;
    }

    public IGridHost getSecurity() {
        if (host().world().isRemote) {
            return null;
        }

        ItemStack component = getComponent();
        if (component == null) {
            return null;
        }
        IGridHost security = (IGridHost) AEApi.instance().registries().locatable().getLocatableBy(getAEKey(component));

        if (checkRange(component, security)) {
            return security;
        }

        return null;
    }

    public boolean checkRange(ItemStack itemStack, IGridHost security) {
        if (itemStack == null || security == null) {
            return false;
        }

        IGridNode gridNode = security.getGridNode(AEPartLocation.INTERNAL);
        if (gridNode == null) {
            return false;
        }

        IGrid grid = gridNode.getGrid();
        if (grid == null) {
            return false;
        }

        switch (itemStack.getItemDamage()) {
            case 0: {
                Class<? extends TileEntity> wirelessAccessPoint = AEApi.instance().definitions().blocks().wirelessAccessPoint().maybeEntity().get();

                return grid.getMachines((Class<? extends IGridHost>) wirelessAccessPoint).iterator().hasNext();
            }
            case 1: {
                IGridBlock gridBlock = gridNode.getGridBlock();
                if (gridBlock == null) {
                    return false;
                }

                    DimensionalCoord location = gridBlock.getLocation();
                if (location == null) {
                    return false;
                }

                Class<? extends TileEntity> accessPoints = AEApi.instance().definitions().blocks().wirelessAccessPoint().maybeEntity().get();

                for (IGridNode node : grid.getMachines((Class<? extends IGridHost>) accessPoints)) {
                    Agent agent = getAgent();
                    IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node;

                    WorldCoord distance = accessPoint.getLocation().subtract((int) agent.xPosition(), ((int) agent.yPosition()), ((int) agent.zPosition()));
                    double squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;

                    double range = accessPoint.getRange();

                    if (squaredDistance <= range * range) {
                        return true;
                    }
                }

                return false;
            }
            default: {
                IGridBlock gridBlock = gridNode.getGridBlock();
                if (gridBlock == null) {
                    return false;
                }

                DimensionalCoord location = gridBlock.getLocation();
                if (location == null) {
                    return false;
                }

                Class<? extends TileEntity> accessPoints = AEApi.instance().definitions().blocks().wirelessAccessPoint().maybeEntity().get();

                for (IGridNode node : grid.getMachines((Class<? extends IGridHost>) accessPoints)) {
                    Agent agent = getAgent();
                    IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node;

                    WorldCoord distance = accessPoint.getLocation().subtract(((int) agent.xPosition()), ((int) agent.yPosition()), ((int) agent.zPosition()));
                    double squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;

                    double range = accessPoint.getRange() / 2;

                    if (squaredDistance <= range * range) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public IGrid getGrid() {
        if (host().world().isRemote) {
            return null;
        }

        IGridHost security = getSecurity();
        if (security == null) {
            return null;
        }

        IGridNode gridNode = security.getGridNode(AEPartLocation.INTERNAL);
        if (gridNode == null) {
            return null;
        }

        return gridNode.getGrid();
    }

    public long getAEKey(ItemStack itemStack) {
        try {
            return Long.parseLong(WirelessHandlerUpgradeAE.instance.getEncryptionKey(itemStack));
        } catch (Throwable e) {
            // Do nothing
        }

        return 0L;
    }

    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        IGrid grid = getGrid();
        if (grid == null) {
            return null;
        }

        return grid.getCache(IStorageGrid.class);
    }

    public IMEMonitor<IAEItemStack> getItemInventory() {
        IGrid grid = getGrid();
        if (grid == null) {
            return null;
        }

        return grid.getCache(IStorageGrid.class);
    }

}
