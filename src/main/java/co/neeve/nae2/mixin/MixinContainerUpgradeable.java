package co.neeve.nae2.mixin;

import appeng.container.implementations.ContainerUpgradeable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ContainerUpgradeable.class)
public class MixinContainerUpgradeable extends MixinAEBaseContainer {
}
