package co.neeve.nae2.common.slots;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;
import co.neeve.nae2.common.interfaces.IContainerPatternMultiTool;
import co.neeve.nae2.common.interfaces.IPatternMultiToolHost;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;

public class SlotPatternMultiTool extends SlotRestrictedInput {
	private final IPatternMultiToolHost host;
	private final int groupNum;
	private boolean allowEdit = true;

	public SlotPatternMultiTool(IItemHandler i, IPatternMultiToolHost host, int slotIndex, int x, int y, int grp,
	                            InventoryPlayer p) {
		super(host instanceof IContainerPatternMultiTool cmp && cmp.isViewingInterface() ?
			PlacableItemType.ENCODED_PATTERN : PlacableItemType.PATTERN, i, slotIndex, x, y, p);
		this.host = host;
		this.groupNum = grp;

		this.setReturnAsSingleStack(false);
	}

	private boolean isAllowEdit() {
		return this.allowEdit;
	}

	public void setAllowEdit(boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	@Override
	public boolean isItemValid(ItemStack i) {
		if (this.host instanceof IContainerPatternMultiTool cmp && cmp.isViewingInterface()) {
			return super.isItemValid(i);
		}

		if (!this.isSlotEnabled()) {
			return false;
		} else if (!this.getContainer().isValidForSlot(this, i)) {
			return false;
		} else if (i.isEmpty()) {
			return false;
		} else if (i.getItem() == Items.AIR) {
			return false;
		} else if (!this.isAllowEdit()) {
			return false;
		} else {
			var definitions = AEApi.instance().definitions();
			var materials = definitions.materials();
			if (i.getItem() instanceof ICraftingPatternItem) {
				return true;
			}

			return materials.blankPattern().isSameAs(i);
		}
	}

	public ItemStack getDisplayStack() {
		if (!Platform.isClient()) return super.getStack();

		var is = super.getStack();

		if (!is.isEmpty() && is.getItem() instanceof ICraftingPatternItem cpi) {
			if (is.getItem() instanceof ItemEncodedPattern iep) {
				return iep.getOutput(is);
			}

			var details = cpi.getPatternForItem(is, null);
			var stack = Arrays.stream(details.getOutputs()).findFirst().orElse(null);

			if (stack != null) {
				return stack.createItemStack();
			}
		}

		return super.getStack();
	}

	@Override
	public boolean isSlotEnabled() {
		return this.host != null && this.host.isPatternRowEnabled(this.groupNum);
	}
}
