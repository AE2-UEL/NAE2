package co.neeve.nae2.mixin.patternmultitool.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.render.StackSizeRenderer;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.client.gui.PatternMultiToolGUIHelper;
import co.neeve.nae2.client.gui.buttons.PatternMultiToolButton;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.common.enums.PatternMultiToolActions;
import co.neeve.nae2.common.interfaces.IExtendedAEItemStack;
import co.neeve.nae2.common.interfaces.IPatternMultiToolToolboxHost;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

@SuppressWarnings({ "SameReturnValue", "AddedMixinMembersNamePattern" })
@Mixin(value = AEBaseGui.class)
public class MixinAEBaseGui extends GuiContainer {
	@Unique
	protected List<PatternMultiToolButton> patternMultiToolButtons = null;

	@Final
	@Shadow(remap = false)
	private StackSizeRenderer stackSizeRenderer;

	public MixinAEBaseGui(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@WrapOperation(method = "drawSlot", at = @At(
		value = "INVOKE",
		target = "Lappeng/util/item/AEItemStack;fromItemStack(Lnet/minecraft/item/ItemStack;)" +
			"Lappeng/util/item/AEItemStack;",
		remap = false
	))
	private static AEItemStack injectItemStack(ItemStack itemStack, Operation<AEItemStack> original, Slot slot) {
		var stack = original.call(itemStack);
		var slotIs = slot.getStack();

		if (stack instanceof IExtendedAEItemStack eais && slotIs.getItem() instanceof ItemEncodedPattern)
			eais.setExtendedCount(slotIs.getCount());

		return stack;
	}

	@Unique
	protected void initializePatternMultiTool() {
		if (this instanceof IPatternMultiToolHostGui host && host.getPMTObject() != null) {
			// Calculate start position for buttons
			var inventorySlots = this.inventorySlots;
			if (inventorySlots == null) return;
			if (inventorySlots instanceof IPatternMultiToolToolboxHost pmh) {
				var offsetX = this.guiLeft + pmh.getPatternMultiToolToolboxOffsetX() - 1;
				var offsetY = this.guiTop + pmh.getPatternMultiToolToolboxOffsetY() - 1;

				offsetY = offsetY + 9 * 18 + 3;

				if (this.patternMultiToolButtons == null) this.patternMultiToolButtons = new ArrayList<>();
				this.patternMultiToolButtons.clear();

				// Add buttons to the GUI
				this.patternMultiToolButtons.add(new PatternMultiToolButton(offsetX, offsetY,
					PatternMultiToolActions.MUL2));
				this.patternMultiToolButtons.add(new PatternMultiToolButton(offsetX + 18, offsetY,
					PatternMultiToolActions.MUL3));
				this.patternMultiToolButtons.add(new PatternMultiToolButton(offsetX + 18 * 2, offsetY,
					PatternMultiToolActions.ADD));

				PatternMultiToolButton button;
				this.patternMultiToolButtons.add(button = new PatternMultiToolButton(offsetX + 18 * 3, offsetY,
					PatternMultiToolActions.CLEAR));
				button.setOverrideName("X");

				this.buttonList.addAll(this.patternMultiToolButtons);
			}
		}
	}

	@Shadow(remap = false)
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
	}

	@Shadow(remap = false)
	public List<Rectangle> getJEIExclusionArea() {
		return null;
	}

	@Inject(method = "getJEIExclusionArea", at = @At("RETURN"), remap = false, cancellable = true)
	public void getJEIExclusionArea(CallbackInfoReturnable<List<Rectangle>> cir) {
		if (this instanceof IPatternMultiToolHostGui pmh && pmh.getPMTObject() != null) {
			var returnValue = cir.getReturnValue();
			var areas = PatternMultiToolGUIHelper.getJEIExclusionArea(pmh);
			if (returnValue != EMPTY_LIST) returnValue.addAll(areas);
			else cir.setReturnValue(areas);
		}
	}

	@Override
	protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop) {
		if (this instanceof IPatternMultiToolHostGui pmh) {
			var outside = PatternMultiToolGUIHelper.hasClickedOutside(pmh, mouseX, mouseY);
			if (!outside) return false;
		}

		return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop);
	}
}