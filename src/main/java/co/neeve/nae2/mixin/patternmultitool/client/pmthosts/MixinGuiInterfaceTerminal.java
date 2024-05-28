package co.neeve.nae2.mixin.patternmultitool.client.pmthosts;

import appeng.client.gui.implementations.GuiInterfaceTerminal;
import co.neeve.nae2.client.gui.PatternMultiToolGUIHelper;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.common.slots.IPMTSlot;
import co.neeve.nae2.mixin.patternmultitool.client.MixinAEBaseGui;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(GuiInterfaceTerminal.class)
public class MixinGuiInterfaceTerminal extends MixinAEBaseGui implements IPatternMultiToolHostGui {
	public MixinGuiInterfaceTerminal(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Inject(method = "drawBG(IIII)V", at = @At("RETURN"), remap
		= false)
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY, CallbackInfo ci) {
		if (this.getPMTObject() != null) {
			PatternMultiToolGUIHelper.drawPMTGui(this, offsetX, offsetY);
		}
	}

	@SuppressWarnings("rawtypes")
	@WrapOperation(
		method = "repositionSlots",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
		),
		remap = false
	)
	private Object wrapRepositionSlots(Iterator self, Operation<Object> operation) {
		var next = operation.call(self);
		if (next instanceof IPMTSlot slotPatternMultiTool) {
			slotPatternMultiTool.setY(11 + slotPatternMultiTool.getInitialY());
			if (self.hasNext()) {
				return this.wrapRepositionSlots(self, operation);
			}
			return null;
		}
		return next;
	}

	@Inject(method = "getJEIExclusionArea", at = @At("RETURN"), remap = false)
	public void injectJEIAreas(CallbackInfoReturnable<List<Rectangle>> cir) {
		cir.getReturnValue().addAll(super.getJEIExclusionArea());
	}

	@Inject(method = "drawFG", at = @At("RETURN"), remap = false)
	public void repositionButtons(CallbackInfo ci) {
		if (this.patternMultiToolButtons != null) {
			PatternMultiToolGUIHelper.repositionButtons(this.patternMultiToolButtons, 0, 11);
		}
	}

	@Override
	public int getPMTOffsetX() {
		return -63 - 18 - 7 - 1 - 9;
	}

	@Override
	public int getPMTOffsetY() {
		return 43 + 16 - 7 - 1 + 10;
	}

	@Inject(method = "drawScreen",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;clear()V",
			shift = At.Shift.AFTER
		), remap = false
	)
	public void injectDrawScreen(CallbackInfo ci) {
		if (this.patternMultiToolButtons != null) {
			this.buttonList.addAll(this.patternMultiToolButtons);
		}
	}

	@Inject(method = "initGui", at = @At("RETURN"), remap = false)
	private void injectButtons(CallbackInfo ci) {
		this.initializePatternMultiTool();
	}
}
