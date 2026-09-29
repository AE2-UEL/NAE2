package co.neeve.nae2.common.integration.opencomputer;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.util.Platform;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.items.ItemMEUpgrade;
import li.cil.oc.common.item.data.DroneData;
import li.cil.oc.common.item.data.RobotData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class WirelessHandlerUpgradeAE implements IWirelessTermHandler {

    public static final WirelessHandlerUpgradeAE INSTANCE = new WirelessHandlerUpgradeAE();

    @Override
    public boolean canHandle(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;

        if (itemStack.getItem() instanceof ItemMEUpgrade) return true;

        return (OCUtils.isRobot(itemStack) && !OCUtils.getComponent(new RobotData(itemStack), NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null)).isEmpty())
                || (OCUtils.isDrone(itemStack) && !OCUtils.getComponent(new DroneData(itemStack), NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null)).isEmpty());
    }

    @Override
    public boolean usePower(EntityPlayer entityPlayer, double v, ItemStack itemStack) {
        return false;
    }

    @Override
    public IConfigManager getConfigManager(ItemStack itemStack) {
        return null;
    }

    @Override
    public IGuiHandler getGuiHandler(ItemStack itemStack) {
        return null;
    }

    @Override
    public boolean hasPower(EntityPlayer entityPlayer, double v, ItemStack itemStack) {
        return true;
    }

    @Override
    public void setEncryptionKey(ItemStack itemStack, String encKey, String name) {
        if (itemStack == null || itemStack.isEmpty()) return;

        if (OCUtils.isRobot(itemStack)) {
            setEncryptionKeyRobot(itemStack, encKey, name);
            return;
        }
        if (OCUtils.isDrone(itemStack)) {
            setEncryptionKeyDrone(itemStack, encKey, name);
            return;
        }

        NBTTagCompound tag = Platform.openNbtData(itemStack);
        tag.setString("encryptionKey", encKey);
        tag.setString("name", name);
    }

    @Override
    public String getEncryptionKey(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return "";

        if (OCUtils.isRobot(itemStack)) {
            return getEncryptionKeyRobot(itemStack);
        }
        if (OCUtils.isDrone(itemStack)) {
            return getEncryptionKeyDrone(itemStack);
        }

        NBTTagCompound tag = Platform.openNbtData(itemStack);

        return tag.getString("encryptionKey");
    }

    private void setEncryptionKeyRobot(ItemStack itemStack, String encKey, String name) {
        RobotData robot = new RobotData(itemStack);
        ItemStack component = OCUtils.getComponent(robot, NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null));
        if (!component.isEmpty()) {
            setEncryptionKey(component, encKey, name);
        }
        robot.save(itemStack);
    }

    private String getEncryptionKeyRobot(ItemStack stack) {
        RobotData robot = new RobotData(stack);
        ItemStack component = OCUtils.getComponent(robot, NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null));
        if (component.isEmpty()) return "";
        return getEncryptionKey(component);
    }

    private void setEncryptionKeyDrone(ItemStack itemStack, String encKey, String name) {
        DroneData drone = new DroneData(itemStack);
        ItemStack component = OCUtils.getComponent(drone, NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null));
        if (!component.isEmpty()) {
            setEncryptionKey(component, encKey, name);
        }
        drone.save(itemStack);
    }

    private String getEncryptionKeyDrone(ItemStack stack) {
        DroneData drone = new DroneData(stack);
        ItemStack component = OCUtils.getComponent(drone, NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null));
        if (component.isEmpty()) return "";
        return getEncryptionKey(component);
    }
}
