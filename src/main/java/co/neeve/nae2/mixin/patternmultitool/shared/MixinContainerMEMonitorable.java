package co.neeve.nae2.mixin.patternmultitool.shared;

import appeng.container.implementations.ContainerMEMonitorable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerMEMonitorable.class)
public class MixinContainerMEMonitorable extends MixinAEBaseContainer {}
