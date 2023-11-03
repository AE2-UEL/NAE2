package co.neeve.nae2.mixin.patternmultitool.shared;

import appeng.api.implementations.IUpgradeableHost;
import appeng.container.implementations.ContainerUpgradeable;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContainerUpgradeable.class, remap = false)
public class MixinContainerUpgradeable extends MixinAEBaseContainer {
	@SuppressWarnings("InjectIntoConstructor")
	@Inject(method =
		"Lappeng/container/implementations/ContainerUpgradeable;<init>" + "(Lnet/minecraft/entity/player" +
			"/InventoryPlayer;Lappeng/api/implementations/IUpgradeableHost;)V", at = @At(value = "INVOKE", target =
		"Lappeng/container/implementations/ContainerUpgradeable;bindPlayerInventory" + "(Lnet/minecraft/entity/player" + "/InventoryPlayer;II)V"))
	public void ctor(final InventoryPlayer ip, final IUpgradeableHost te, CallbackInfo cb) {
		this.initializePatternMultiTool();
	}
}
