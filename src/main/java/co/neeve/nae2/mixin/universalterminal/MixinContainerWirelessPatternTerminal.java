package co.neeve.nae2.mixin.universalterminal;

import appeng.container.implementations.ContainerWirelessPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ContainerWirelessPatternTerminal.class, remap = false)
public abstract class MixinContainerWirelessPatternTerminal {

    @Redirect(
            method = "loadFromNBT",
            at = @At(value = "INVOKE", target = "Lappeng/tile/inventory/AppEngInternalInventory;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V", ordinal = 0)
    )
    private void loadFromNBT(AppEngInternalInventory instance, NBTTagCompound data, String name) {
        instance.readFromNBT(data, "craftingGridPattern");
    }

    @Redirect(
            method = "saveChanges",
            at = @At(value = "INVOKE", target = "Lappeng/tile/inventory/AppEngInternalInventory;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V", ordinal = 0)
    )
    public void saveChanges(AppEngInternalInventory instance, NBTTagCompound data, String name) {
        instance.writeToNBT(data, "craftingGridPattern");
    }
}
