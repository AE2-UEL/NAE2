package co.neeve.nae2.common.integration.opencomputer;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import co.neeve.nae2.NAE2;
import li.cil.oc.common.item.data.DroneData;
import li.cil.oc.common.item.data.RobotData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class WirelessHandlerUpgradeAE implements IWirelessTermHandler {

    public final static WirelessHandlerUpgradeAE instance = new WirelessHandlerUpgradeAE();

    @Override
    public String getEncryptionKey(ItemStack itemStack) {
        if (itemStack == ItemStack.EMPTY) {
            return "";
        }

        if (OCUtils.isRobot(itemStack)) {
            return getEncryptionKeyForRobot(itemStack);
        }
        if (OCUtils.isDrone(itemStack)) {
            return getEncryptionKeyForDrone(itemStack);
        }

        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        return itemStack.getTagCompound().getString("key");
    }

    private String getEncryptionKeyForRobot(ItemStack itemStack) {
        RobotData robotData = new RobotData(itemStack);

        ItemStack component = OCUtils.getComponent(robotData, NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null));
        if (component == null) {
            return "";
        }

        return getEncryptionKey(component);
    }

    private String getEncryptionKeyForDrone(ItemStack itemStack) {
        DroneData droneData = new DroneData(itemStack);

        ItemStack component = OCUtils.getComponent(droneData, NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null));
        if (component == null) {
            return "";
        }

        return getEncryptionKey(component);
    }

    @Override
    public void setEncryptionKey(ItemStack itemStack, String encryptionKey, String name) {
        if (itemStack == null) {
            return;
        }
        if (OCUtils.isRobot(itemStack)) {
            setEncryptionKeyForRobot(itemStack, encryptionKey, name);
            return;
        }
        if (OCUtils.isDrone(itemStack)) {
            setEncryptionKeyForDrone(itemStack, encryptionKey, name);
            return;
        }

        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setString("key", encryptionKey);
    }

    private void setEncryptionKeyForRobot(ItemStack itemStack, String encryptionKey, String name) {
        RobotData robot = new RobotData(itemStack);
        ItemStack component = OCUtils.getComponent(robot, NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null));

        if (component != null) {
            setEncryptionKey(component, encryptionKey, name);
        }

        robot.save(itemStack);
    }

    private void setEncryptionKeyForDrone(ItemStack itemStack, String encryptionKey, String name) {
        DroneData drone = new DroneData(itemStack);
        ItemStack component = OCUtils.getComponent(drone, NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null));

        if (component != null) {
            setEncryptionKey(component, encryptionKey, name);
        }

        drone.save(itemStack);
    }

    @Override
    public boolean canHandle(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        Item item = itemStack.getItem();

        if (item == NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null)) {
            return true;
        }

        boolean robotCheck = OCUtils.isRobot(itemStack) && OCUtils.getComponent(new RobotData(itemStack), NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null)) != null;
        boolean droneCheck = OCUtils.isDrone(itemStack) && OCUtils.getComponent(new DroneData(itemStack), NAE2.definitions().items().openComputerUpgrade().maybeItem().orElse(null)) != null;

        return robotCheck || droneCheck;
    }

    @Override
    public boolean usePower(EntityPlayer entityPlayer, double v, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean hasPower(EntityPlayer entityPlayer, double v, ItemStack itemStack) {
        return true;
    }

    @Override
    public IConfigManager getConfigManager(ItemStack itemStack) {
        return null;
    }

    @Override
    public IGuiHandler getGuiHandler(ItemStack p0) {
        // TODO: Not yet implemented
        return GuiBridge.GUI_WIRELESS_TERM;
    }
}

