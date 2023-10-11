package co.neeve.nae2.mixin.base.shared;

import appeng.container.implementations.ContainerMEMonitorable;
import co.neeve.nae2.mixin.patternmultitool.shared.MixinAEBaseContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerMEMonitorable.class)
public class MixinContainerMEMonitorable extends MixinAEBaseContainer {}
