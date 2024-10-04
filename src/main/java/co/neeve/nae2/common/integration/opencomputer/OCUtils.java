package co.neeve.nae2.common.integration.opencomputer;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import li.cil.oc.api.API;
import li.cil.oc.api.detail.ItemInfo;
import li.cil.oc.common.item.data.DroneData;
import li.cil.oc.common.item.data.RobotData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OCUtils {
    public static <P extends IPart, C extends P> P getPart(World world, BlockPos pos, AEPartLocation location, Class<C> clazz) {
        if (world == null || pos == null) {
            return null;
        }

        TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof IPartHost)) {
            return null;
        }

        if (location == null || location == AEPartLocation.INTERNAL) {
            for (AEPartLocation side : AEPartLocation.SIDE_LOCATIONS) {
                IPart part = ((IPartHost) tile).getPart(side);

                if (clazz.isInstance(part)) {
                    return (P) part;
                }
            }

            return null;
        }

        IPart part = ((IPartHost) tile).getPart(location);

        if (!clazz.isInstance(part)) {
            return null;
        }

        return (P) part;
    }

    public static boolean isRobot(ItemStack itemStack) {
        ItemInfo item = API.items.get(itemStack);

        return item != null && "robot".equals(item.name());
    }

    public static boolean isDrone(ItemStack itemStack) {
        ItemInfo item = API.items.get(itemStack);

        return item != null && "drone".equals(item.name());
    }

    public static ItemStack getComponent(RobotData robot, Item item, int meta) {
        for (ItemStack component : robot.components()) {
            if (component != null && component.getItem() == item) {
                return component;
            }
        }

        return null;
    }

    public static ItemStack getComponent(RobotData robot, Item item) {
        return getComponent(robot, item, 0);
    }

    public static ItemStack getComponent(DroneData drone, Item item, int meta) {
        for (ItemStack component : drone.components()) {
            if (component != null && component.getItem() == item) {
                return component;
            }
        }

        return null;
    }

    public static ItemStack getComponent(DroneData drone, Item item) {
        return getComponent(drone, item, 0);
    }
}
