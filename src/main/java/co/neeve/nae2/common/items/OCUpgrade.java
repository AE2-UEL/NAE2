package co.neeve.nae2.common.items;

import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.integration.opencomputer.UpgradeAE;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.internal.Drone;
import li.cil.oc.api.internal.Robot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList(value = {
        @Optional.Interface(iface = "li.cil.oc.api.driver.item.HostAware", modid = "opencomputers", striprefs = true),
        @Optional.Interface(iface = "li.cil.oc.api.driver.EnvironmentProvider", modid = "opencomputers", striprefs = true)
})
public class OCUpgrade extends Item implements HostAware, EnvironmentProvider {
    public OCUpgrade() {
        setHasSubtypes(true);
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public boolean worksWith(ItemStack itemStack, Class<? extends EnvironmentHost> host) {
        return worksWith(itemStack) && host != null && (Robot.class.isAssignableFrom(host) || Drone.class.isAssignableFrom(host));
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public boolean worksWith(ItemStack itemStack) {
        return itemStack != ItemStack.EMPTY && itemStack.getItem() == this;
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public ManagedEnvironment createEnvironment(ItemStack itemStack, EnvironmentHost environmentHost) {
        if (environmentHost == null) {
            return null;
        }
        if (itemStack != ItemStack.EMPTY && itemStack.getItem() == this && worksWith(itemStack, environmentHost.getClass())) {
            return new UpgradeAE(environmentHost);
        }

        return null;
    }

    @Override
    public String slot(ItemStack itemStack) {
        return Slot.Upgrade;
    }

    @Override
    public int tier(ItemStack itemStack) {
        return switch (itemStack.getItemDamage()) {
            case 0 -> 2;
            case 1 -> 1;
            default -> 0;
        };
    }

    @Override
    public NBTTagCompound dataTag(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound nbt = itemStack.getTagCompound();

        if (!nbt.hasKey("oc:data")) {
            nbt.setTag("oc:data", new NBTTagCompound());
        }

        return nbt.getCompoundTag("oc:data");
    }

    @Override
    public Class<?> getEnvironment(ItemStack itemStack) {
        if (itemStack != ItemStack.EMPTY && itemStack.getItem() == this) {
            return UpgradeAE.class;
        }

        return null;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }

        items.add(new ItemStack(this, 1, 2));
        items.add(new ItemStack(this, 1, 1));
        items.add(new ItemStack(this, 1, 0));
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int itemDamage = stack.getItemDamage();

        return super.getItemStackDisplayName(stack) + String.format("(Tier %d )", itemDamage+1);
    }
}
