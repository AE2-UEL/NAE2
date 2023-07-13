package co.neeve.nae2.mixin.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.render.StackSizeRenderer;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.client.gui.PatternMultiToolGUIHelper;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.common.interfaces.IExtendedAEItemStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

@SuppressWarnings("SameReturnValue")
@Mixin(value = AEBaseGui.class)
public class MixinAEBaseGui extends GuiContainer {
	@Final
	@Shadow
	private StackSizeRenderer stackSizeRenderer;

	public MixinAEBaseGui(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	@WrapOperation(method = "drawSlot", at = @At(value = "INVOKE", target = "Lappeng/util/item/AEItemStack;" +
		"fromItemStack(Lnet/minecraft/item/ItemStack;)Lappeng/util/item/AEItemStack;"))
	private static AEItemStack injectItemStack(ItemStack itemStack, Operation<AEItemStack> original, Slot slot) {
		AEItemStack stack = original.call(itemStack);
		ItemStack slotIs = slot.getStack();

		if (stack instanceof IExtendedAEItemStack eais && slotIs.getItem() instanceof ItemEncodedPattern)
			eais.setExtendedCount(slotIs.getCount());

		return stack;
	}

	@Shadow
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
	}

	@Shadow
	protected void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
	}

	@Shadow
	protected List<Rectangle> getJEIExclusionArea() {
		return null;
	}

	@Inject(method = "getJEIExclusionArea", at = @At("RETURN"), remap = false, cancellable = true)
	public void getJEIExclusionArea(CallbackInfoReturnable<List<Rectangle>> cir) {
		if (this instanceof IPatternMultiToolHostGui pmh && pmh.getPMTObject() != null) {
			List<Rectangle> returnValue = cir.getReturnValue();
			List<Rectangle> areas = PatternMultiToolGUIHelper.getJEIExclusionArea(pmh);
			if (returnValue != EMPTY_LIST) returnValue.addAll(areas);
			else cir.setReturnValue(areas);
		}
	}

	@Override
	protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft, int guiTop) {
		if (this instanceof IPatternMultiToolHostGui pmh) {
			boolean outside = PatternMultiToolGUIHelper.hasClickedOutside(pmh, mouseX, mouseY, guiLeft, guiTop);
			if (!outside) return false;
		}

		return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop);
	}
}