package co.neeve.nae2.mixin.patternmultitool.shared.pmthosts;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerPatternTerm;
import co.neeve.nae2.common.interfaces.IPatternMultiToolToolboxHost;
import co.neeve.nae2.mixin.patternmultitool.shared.MixinContainerPatternEncoder;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = ContainerPatternTerm.class, remap = false)
public class MixinContainerPatternTerm extends MixinContainerPatternEncoder implements IPatternMultiToolToolboxHost {
	@SuppressWarnings("InjectIntoConstructor")
	@Inject(method =
		"Lappeng/container/implementations/ContainerPatternTerm;<init>(Lnet/minecraft/entity/player/InventoryPlayer;" +
			"Lappeng/api/storage/ITerminalHost;)V", at = @At(value = "INVOKE", target = "Lappeng" +
		"/container/implementations/ContainerPatternTerm;bindPlayerInventory" + "(Lnet/minecraft/entity/player" +
		"/InventoryPlayer;II)V"))
	public void ctor(final InventoryPlayer ip, final ITerminalHost monitorable, CallbackInfo ci) {
		this.initializePatternMultiTool();
	}

	@Override
	public int getPatternMultiToolToolboxOffsetX() {
		return -63 - 18;
	}

	@Override
	public int getPatternMultiToolToolboxOffsetY() {
		return 43 + 16;
	}
}
