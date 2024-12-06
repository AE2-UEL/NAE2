package co.neeve.nae2.mixin.universalterminal;

import appeng.container.implementations.ContainerWirelessPatternTerminal;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.tile.inventory.AppEngInternalInventory;
import co.neeve.nae2.NAE2;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ContainerWirelessPatternTerminal.class, remap = false)
public abstract class MixinContainerWirelessPatternTerminal {

    @Shadow
    @Final
    private WirelessTerminalGuiObject wirelessTerminalGUIObject;

    @Redirect(
            method = "loadFromNBT",
            at = @At(value = "INVOKE", target = "Lappeng/tile/inventory/AppEngInternalInventory;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V", ordinal = 0)
    )
    private void loadFromNBT(AppEngInternalInventory instance, NBTTagCompound data, String name) {
        if (NAE2.definitions().items().universalWirelessTerminal().isSameAs(wirelessTerminalGUIObject.getItemStack())) {
            instance.readFromNBT(data, "craftingGridPattern");
        } else {
            instance.readFromNBT(data, name);
        }
    }

    @Redirect(
            method = "saveChanges",
            at = @At(value = "INVOKE", target = "Lappeng/tile/inventory/AppEngInternalInventory;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;Ljava/lang/String;)V", ordinal = 0)
    )
    public void saveChanges(AppEngInternalInventory instance, NBTTagCompound data, String name) {
        if (NAE2.definitions().items().universalWirelessTerminal().isSameAs(wirelessTerminalGUIObject.getItemStack())) {
            instance.writeToNBT(data, "craftingGridPattern");
        } else {
            instance.writeToNBT(data, name);
        }

    }
}
