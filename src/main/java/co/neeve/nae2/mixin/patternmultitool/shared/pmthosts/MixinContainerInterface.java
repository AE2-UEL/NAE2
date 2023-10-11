package co.neeve.nae2.mixin.patternmultitool.shared.pmthosts;

import appeng.container.implementations.ContainerInterface;
import co.neeve.nae2.common.interfaces.IPatternMultiToolToolboxHost;
import co.neeve.nae2.mixin.patternmultitool.shared.MixinContainerUpgradeable;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = ContainerInterface.class)
public class MixinContainerInterface extends MixinContainerUpgradeable implements IPatternMultiToolToolboxHost {
	@Override
	public int getPatternMultiToolToolboxOffsetX() {
		return -63 - 18;
	}

	@Override
	public int getPatternMultiToolToolboxOffsetY() {
		return 43 + 16 + 2;
	}
}
