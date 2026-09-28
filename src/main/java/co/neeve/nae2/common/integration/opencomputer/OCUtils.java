package co.neeve.nae2.common.integration.opencomputer;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import li.cil.oc.api.Items;
import li.cil.oc.api.detail.ItemInfo;
import li.cil.oc.common.item.data.DroneData;
import li.cil.oc.common.item.data.RobotData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OCUtils {

    @SuppressWarnings("unchecked")
    public static <P extends IPart, C extends Class<? extends P>> P getPart(World world, BlockPos pos, AEPartLocation location, C clazz) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof IPartHost host)) return null;

        if (location == null || location == AEPartLocation.INTERNAL) {
            for (AEPartLocation side : AEPartLocation.SIDE_LOCATIONS) {
                IPart part = host.getPart(side);
                if (clazz.isInstance(part)) {
                    return (P) part;
                }
            }
            return null;
        } else {
            IPart part = host.getPart(location);
            if (clazz.isInstance(part)) {
                return (P) part;
            }
            return null;
        }
    }

    public static boolean isRobot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemInfo item = Items.get(stack);
        return item != null && "robot".equals(item.name());
    }

    public static boolean isDrone(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemInfo item = Items.get(stack);
        return item != null && "drone".equals(item.name());
    }

    public static ItemStack getComponent(RobotData robot, Item item) {
        if (item == null) return ItemStack.EMPTY;

        return getComponent(robot, item, 0);
    }

    public static ItemStack getComponent(RobotData robot, Item item, int meta) {
        if (robot == null || robot.components() == null) return ItemStack.EMPTY;
        for (ItemStack component : robot.components()) {
            if (component != null && !component.isEmpty() && component.getItem() == item) {
                return component;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getComponent(DroneData drone, Item item) {
        return getComponent(drone, item, 0);
    }

    public static ItemStack getComponent(DroneData drone, Item item, int meta) {
        if (drone == null || drone.components() == null) return ItemStack.EMPTY;
        for (ItemStack component : drone.components()) {
            if (component != null && !component.isEmpty() && component.getItem() == item) {
                return component;
            }
        }
        return ItemStack.EMPTY;
    }
}
