package co.neeve.nae2.mixin.client;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.AppEngSlot;
import co.neeve.nae2.client.gui.PatternMultiToolGUIHelper;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.common.slots.SlotPatternMultiTool;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static co.neeve.nae2.client.gui.PatternMultiToolGUIHelper.PMT_HEIGHT;

@Mixin(GuiPatternTerm.class)
public class MixinGuiPatternTerminal extends MixinGuiMEMonitorable implements IPatternMultiToolHostGui {

	public MixinGuiPatternTerminal(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
		super.drawBG(offsetX, offsetY, mouseX, mouseY);

		if (this.getPMTObject() != null) {
			PatternMultiToolGUIHelper.drawPMTGui(this, offsetX, offsetY);
		}
	}

	@Inject(method = "repositionSlot(Lappeng/container/slot/AppEngSlot;)V", at = @At(value = "HEAD"), remap = false,
		cancellable = true)
	public void repositionSlot(final AppEngSlot s, CallbackInfo ci) {
		if (s instanceof SlotPatternMultiTool) {
			s.yPos = s.getY() + this.getPMTOffsetY() - 50;
			ci.cancel();
		}
	}

	@Override
	public int getPMTOffsetX() {
		return -63 - 18 - 7 - 1;
	}

	@Override
	public int getPMTOffsetY() {
		return this.ySize - PMT_HEIGHT - 1;
	}
}
