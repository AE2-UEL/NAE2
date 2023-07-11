package co.neeve.nae2.mixin;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.util.IConfigManagerHost;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerMEMonitorable.class)
public class MixinContainerMEMonitorable extends MixinAEBaseContainer implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEItemStack> {
    @Shadow
    public boolean isValid(Object verificationToken) {
        return false;
    }

    @Shadow
    public void postChange(IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, IActionSource actionSource) {
    }

    @Shadow
    public void onListUpdate() {
    }

    @Shadow
    public IConfigManager getConfigManager() {
        return null;
    }

    @Shadow
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
    }

    @Shadow
    public IGridNode getNetworkNode() {
        return null;
    }

    @Shadow
    public ItemStack[] getViewCells() {
        return new ItemStack[0];
    }
}
