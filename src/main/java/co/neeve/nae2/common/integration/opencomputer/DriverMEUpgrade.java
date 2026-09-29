package co.neeve.nae2.common.integration.opencomputer;

import co.neeve.nae2.NAE2;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.internal.Agent;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.integration.opencomputers.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DriverMEUpgrade implements DriverItem, EnvironmentProvider {

    @Override
    public boolean worksWith(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == NAE2.definitions().items().ocUpgrade().maybeItem().orElse(null);
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        return host instanceof Agent ? new EnvironmentMEUpgrade(host) : null;
    }

    @Override
    public String slot(ItemStack stack) {
        return Slot.Upgrade;
    }

    @Override
    public int tier(ItemStack stack) {
        return switch (stack.getItemDamage()) {
            case 0 -> 2; // Tier 3
            case 1 -> 1; // Tier 2
            default -> 0; // Tier 1
        };
    }

    @Override
    public NBTTagCompound dataTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey("oc:data")) {
            nbt.setTag("oc:data", new NBTTagCompound());
        }

        return nbt.getCompoundTag("oc:data");
    }

    @Override
    public Class<?> getEnvironment(ItemStack itemStack) {
        if (worksWith(itemStack)) {
            return EnvironmentMEUpgrade.class;
        }
        return null;
    }
}
