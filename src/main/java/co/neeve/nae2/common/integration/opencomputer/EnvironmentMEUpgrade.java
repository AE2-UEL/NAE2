package co.neeve.nae2.common.integration.opencomputer;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.features.ILocatable;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.MachineSource;
import appeng.tile.networking.TileWireless;
import co.neeve.nae2.NAE2;
import li.cil.oc.api.Network;
import li.cil.oc.api.internal.Agent;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.internal.Drone;
import li.cil.oc.api.internal.MultiTank;
import li.cil.oc.api.internal.Robot;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;

public class EnvironmentMEUpgrade extends AbstractManagedEnvironment {

    private final EnvironmentHost host;
    private final Agent agent;
    private final Robot robot;
    private final Drone drone;
    private boolean isActive = false;

    public EnvironmentMEUpgrade(EnvironmentHost host) {
        this.host = host;
        this.agent = host instanceof Agent ? (Agent) host : null;
        this.robot = host instanceof Robot ? (Robot) host : null;
        this.drone = host instanceof Drone ? (Drone) host : null;

        setNode(Network.newNode(this, Visibility.Network)
                .withComponent("upgrade_me",Visibility.Neighbors)
                .withConnector()
                .create());
    }

    public ItemStack getComponent() {
        if (robot != null && node() != null) {
            int slot = robot.componentSlot(node().address());
            if (slot >= 0) {
                return robot.getStackInSlot(slot);
            }
        } else if (drone != null) {
            for (ItemStack item : drone.internalComponents()) {
                if (item != null && !item.isEmpty() && item.getItem() == NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null)) {
                    return item;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public IGridHost getSecurityTerminal() {
        if (host.world().isRemote) return null;
        ItemStack stack = getComponent();
        if (stack.isEmpty()) return null;

        String key = WirelessHandlerUpgradeAE.INSTANCE.getEncryptionKey(stack);
        if (key.isEmpty()) return null;

        try {
            long locatableId = Long.parseLong(key);
            ILocatable locatable = AEApi.instance().registries().locatable().getLocatableBy(locatableId);
            if (locatable instanceof IGridHost actionHost) {
                return checkRange(stack, actionHost) ? actionHost : null;
            }
        } catch (NumberFormatException ignored) {}
        return null;
    }

    public boolean checkRange(ItemStack stack, IGridHost sec) {
        if (sec == null || agent == null) return false;
        IGridNode gridNode = sec.getGridNode(AEPartLocation.INTERNAL);
        if (gridNode == null) return false;
        IGrid grid = gridNode.getGrid();

        IMachineSet wirelessMachines = grid.getMachines(TileWireless.class);
        if (wirelessMachines.isEmpty()) return false;

        int damage = stack.getItemDamage();

        for (IGridNode n : wirelessMachines) {
            if (n.getMachine() instanceof IWirelessAccessPoint wap) {
                if (damage == 0) {
                    if (wap.isActive()) return true;
                    continue;
                }
                if (testWap(wap, agent, damage)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean testWap(IWirelessAccessPoint wap, Agent agent, int damage) {
        if (!wap.isActive()) return false;

        DimensionalCoord dc = wap.getLocation();
        if (dc.getWorld().provider.getDimension() != agent.world().provider.getDimension()) {
            return false;
        }

        double offX = (double) dc.x - agent.xPosition();
        double offY = (double) dc.y - agent.yPosition();
        double offZ = (double) dc.z - agent.zPosition();
        double r = offX * offX + offY * offY + offZ * offZ;

        double range = wap.getRange();
        if (damage != 1) {
            range /= 2.0;
        }
        return r < (range * range);
    }

    public IGrid getGrid() {
        if (host.world().isRemote) return null;
        IGridHost sec = getSecurityTerminal();
        if (sec == null) return null;
        IGridNode node = sec.getGridNode(AEPartLocation.INTERNAL);
        return node != null ? node.getGrid() : null;
    }

    public IMEMonitor<IAEItemStack> getItemInventory() {
        IGrid grid = getGrid();
        if (grid == null) return null;
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        return storage.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
    }

    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        IGrid grid = getGrid();
        if (grid == null) return null;
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        return storage.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    }

    private IActionSource getActionSource() {
        IGridHost sec = getSecurityTerminal();
        if (sec instanceof IActionHost actionHost) {
            return new MachineSource(actionHost);
        }
        return null;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void update() {
        super.update();
        if (host.world().getTotalWorldTime() % 10 == 0 && isActive) {
            if (node() instanceof Connector connector) {
                if (!connector.tryChangeBuffer(-getEnergy())) {
                    isActive = false;
                }
            }
        }
    }

    public double getEnergy() {
        ItemStack c = getComponent();
        if (c.isEmpty()) return 0.0;
        return switch (c.getItemDamage()) {
            case 0 -> 0.6;  // Tier 3
            case 1 -> 0.3;  // Tier 2
            default -> 0.05; // Tier 1
        };
    }

    @Override
    public void onMessage(Message message) {
        super.onMessage(message);
        if ("computer.stopped".equals(message.name())) {
            isActive = false;
        } else if ("computer.started".equals(message.name())) {
            isActive = true;
        }
    }

    @Callback(doc = "function():boolean -- Return true if the card is linked to your ae network.")
    public Object[] isLinked(Context context, Arguments args) {
        return new Object[]{getGrid() != null};
    }

    @Callback(doc = "function([number:amount]):number -- Transfer selected items to your ae system.")
    public Object[] sendItems(Context context, Arguments args) {
        if (agent == null) return new Object[]{0};
        int selected = agent.selectedSlot();
        IInventory invRobot = agent.mainInventory();
        if (invRobot.getSizeInventory() <= 0) return new Object[]{0};

        ItemStack stack = invRobot.getStackInSlot(selected);
        IMEMonitor<IAEItemStack> inv = getItemInventory();
        if (stack.isEmpty() || inv == null) return new Object[]{0};

        int amount = Math.min(args.optInteger(0, 64), stack.getCount());
        ItemStack stack2 = stack.copy();
        stack2.setCount(amount);

        IAEItemStack notInjected = inv.injectItems(
                AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack2),
                Actionable.MODULATE,
                getActionSource()
        );

        if (notInjected == null) {
            stack.shrink(amount);
            invRobot.setInventorySlotContents(selected, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return new Object[]{amount};
        } else {
            int injected = amount - (int) notInjected.getStackSize();
            stack.shrink(injected);
            invRobot.setInventorySlotContents(selected, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return new Object[]{injected};
        }
    }

    @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get items from your ae system.")
    public Object[] requestItems(Context context, Arguments args) {
        if (agent == null) return new Object[]{0};
        String address = args.checkString(0);
        int entry = args.checkInteger(1);
        int amount = args.optInteger(2, 64);

        int selected = agent.selectedSlot();
        IInventory invRobot = agent.mainInventory();
        if (invRobot.getSizeInventory() <= 0) return new Object[]{0};

        IMEMonitor<IAEItemStack> inv = getItemInventory();
        if (inv == null) return new Object[]{0};

        Node n = node().network().node(address);
        if (!(n instanceof Component)) throw new IllegalArgumentException("no such component");
        if (!(n.host() instanceof Database database)) throw new IllegalArgumentException("not a database");

        ItemStack template = database.getStackInSlot(entry - 1);
        if (template.isEmpty()) return new Object[]{0};

        ItemStack sel = invRobot.getStackInSlot(selected);
        int inSlot = sel.isEmpty() ? 0 : sel.getCount();
        int maxSize = sel.isEmpty() ? 64 : sel.getMaxStackSize();

        if (!sel.isEmpty() && (!ItemStack.areItemsEqual(sel, template) || !ItemStack.areItemStackTagsEqual(sel, template))) {
            return new Object[]{0};
        }

        int toExtract = Math.min(amount, maxSize - inSlot);
        if (toExtract <= 0) return new Object[]{0};

        ItemStack targetStack = template.copy();
        targetStack.setCount(toExtract);

        IAEItemStack extracted = inv.extractItems(
                AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(targetStack),
                Actionable.MODULATE,
                getActionSource()
        );

        if (extracted == null) return new Object[]{0};

        int ext = (int) extracted.getStackSize();
        if (sel.isEmpty()) {
            ItemStack newStack = template.copy();
            newStack.setCount(ext);
            invRobot.setInventorySlotContents(selected, newStack);
        } else {
            sel.grow(ext);
            invRobot.setInventorySlotContents(selected, sel);
        }

        return new Object[]{ext};
    }

    @Callback(doc = "function([number:amount]):number -- Transfer selecte fluid to your ae system.")
    public Object[] sendFluids(Context context, Arguments args) {
        if (agent == null) return new Object[]{0};
        int selected = agent.selectedTank();
        MultiTank tanks = agent.tank();
        if (tanks.tankCount() <= 0) return new Object[]{0};

        IFluidTank tank = tanks.getFluidTank(selected);
        IMEMonitor<IAEFluidStack> inv = getFluidInventory();
        if (tank == null || inv == null || tank.getFluid() == null) return new Object[]{0};

        int amount = Math.min(args.optInteger(0, tank.getCapacity()), tank.getFluidAmount());
        FluidStack fluid = tank.getFluid().copy();
        fluid.amount = amount;

        IAEFluidStack notInjected = inv.injectItems(
                AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class).createStack(fluid),
                Actionable.MODULATE,
                getActionSource()
        );

        if (notInjected == null) {
            tank.drain(amount, true);
            return new Object[]{amount};
        } else {
            int drained = amount - (int) notInjected.getStackSize();
            tank.drain(drained, true);
            return new Object[]{drained};
        }
    }

    @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get fluid from your ae system.")
    public Object[] requestFluids(Context context, Arguments args) {
        if (agent == null) return new Object[]{0};
        String address = args.checkString(0);
        int entry = args.checkInteger(1);
        int amount = args.optInteger(2, Fluid.BUCKET_VOLUME);

        MultiTank tanks = agent.tank();
        int selected = agent.selectedTank();
        if (tanks.tankCount() <= 0) return new Object[]{0};

        IFluidTank tank = tanks.getFluidTank(selected);
        IMEMonitor<IAEFluidStack> inv = getFluidInventory();
        if (tank == null || inv == null) return new Object[]{0};

        Node n = node().network().node(address);
        if (!(n instanceof Component)) throw new IllegalArgumentException("no such component");
        if (!(n.host() instanceof Database database)) throw new IllegalArgumentException("not a database");

        ItemStack containerStack = database.getStackInSlot(entry - 1);
        if (containerStack.isEmpty()) return new Object[]{0};

        FluidStack fluidFromContainer = FluidUtil.getFluidContained(containerStack);
        if (fluidFromContainer == null) return new Object[]{0};

        fluidFromContainer.amount = amount;
        FluidStack simFilled = fluidFromContainer.copy();
        int fillable = tank.fill(simFilled, false);
        if (fillable <= 0) return new Object[]{0};

        simFilled.amount = fillable;
        IAEFluidStack extracted = inv.extractItems(
                AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class).createStack(simFilled),
                Actionable.MODULATE,
                getActionSource()
        );

        if (extracted == null || extracted.getStackSize() <= 0) return new Object[]{0};

        int actualFilled = tank.fill(extracted.getFluidStack(), true);
        return new Object[]{actualFilled};
    }
}
