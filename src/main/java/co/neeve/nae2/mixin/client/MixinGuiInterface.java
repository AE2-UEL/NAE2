package co.neeve.nae2.mixin.client;

import appeng.client.gui.implementations.GuiInterface;
import co.neeve.nae2.client.gui.PatternMultiToolGUIHelper;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiInterface.class)
public class MixinGuiInterface extends MixinGuiUpgradeable implements IPatternMultiToolHostGui {
	public MixinGuiInterface(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@Override
	@Intrinsic
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
		super.drawBG(offsetX, offsetY, mouseX, mouseY);

		if (this.getPMTObject() != null) {
			PatternMultiToolGUIHelper.drawPMTGui(this, offsetX, offsetY);
		}
	}

	@Override
	public int getPMTOffsetX() {
		return -63 - 18 - 7 - 1;
	}

	@Override
	public int getPMTOffsetY() {
		return 43 + 16 - 7;
	}
}
