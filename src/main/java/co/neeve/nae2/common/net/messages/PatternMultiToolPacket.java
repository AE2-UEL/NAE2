package co.neeve.nae2.common.net.messages;

import appeng.api.AEApi;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.helpers.ItemStackHelper;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import co.neeve.nae2.common.containers.ContainerPatternMultiTool;
import co.neeve.nae2.common.enums.PatternMultiToolActionTypes;
import co.neeve.nae2.common.enums.PatternMultiToolActions;
import co.neeve.nae2.common.enums.PatternMultiToolTabs;
import co.neeve.nae2.common.interfaces.IContainerPatternMultiTool;
import co.neeve.nae2.common.interfaces.IPatternMultiToolHost;
import co.neeve.nae2.common.net.INAEMessage;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

public class PatternMultiToolPacket implements INAEMessage {
	private int value;
	private int actionType;

	public PatternMultiToolPacket() {
	}

	public PatternMultiToolPacket(PatternMultiToolActionTypes actionType, int value) {
		this.actionType = actionType.ordinal();
		this.value = value;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.actionType);
		buf.writeInt(this.value);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.actionType = buf.readInt();
		this.value = buf.readInt();
	}

	public int getValue() {
		return this.value;
	}

	public PatternMultiToolActionTypes getActionType() {
		return PatternMultiToolActionTypes.values()[this.actionType];
	}

	@Override
	public void process(MessageContext ctx) {
		if (Platform.isClient()) return;
		var player = ctx.getServerHandler().player;

		if (player.openContainer instanceof AEBaseContainer bc && bc instanceof IPatternMultiToolHost container) {
			if (this.getActionType() == PatternMultiToolActionTypes.TAB_SWITCH && container instanceof ContainerPatternMultiTool containerPatternMultiTool) {
				containerPatternMultiTool.switchTab(PatternMultiToolTabs.values()[this.getValue()]);
			} else if (this.getActionType() == PatternMultiToolActionTypes.BUTTON_PRESS) {
				var inv = (AppEngInternalInventory) container.getPatternInventory();
				if (inv == null) return;

				var value = PatternMultiToolActions.values()[this.getValue()];

				if (value == PatternMultiToolActions.INV_SWITCH && container instanceof IContainerPatternMultiTool cmp) {
					cmp.toggleInventory();
					bc.detectAndSendChanges();
					return;
				}

				if (value == PatternMultiToolActions.REPLACE && container instanceof IContainerPatternMultiTool cmp) {
					this.searchAndReplace(cmp, player);
					bc.detectAndSendChanges();
					return;
				}

				for (var is : inv) {
					if (is.getItem() instanceof ItemEncodedPattern) {
						try {
							this.handleButtonPress(is, bc, value);
						} catch (IndexOutOfBoundsException e) {
							// uwu
						}
					}
				}

				bc.detectAndSendChanges();
			}
		}
	}

	// Handles the logic for when a button is pressed
	private void handleButtonPress(ItemStack is, AEBaseContainer container, PatternMultiToolActions buttonId) {
		switch (buttonId) {
			case MUL2 -> this.updatePatternCount(is, 2, Operation.MULTIPLY);
			case MUL3 -> this.updatePatternCount(is, 3, Operation.MULTIPLY);
			case ADD -> this.updatePatternCount(is, 1, Operation.ADD);
			case DIV2 -> this.updatePatternCount(is, 2, Operation.DIVIDE);
			case DIV3 -> this.updatePatternCount(is, 3, Operation.DIVIDE);
			case SUB -> this.updatePatternCount(is, 1, Operation.SUBTRACT);
			case CLEAR -> this.emptyPattern(is, container);
		}
	}

	private void emptyPattern(ItemStack is, AEBaseContainer container) {
		var newStack =
			AEApi.instance().definitions().materials().blankPattern().maybeStack(is.getCount()).orElse(ItemStack.EMPTY);

		container.putStackInSlot(container.getInventory().indexOf(is), newStack);
	}

	// Checks if each tag in the list can be divided evenly by the factor
	private boolean canDivideTagList(NBTTagList tagList, int factor, String countTag) {
		for (var tag : tagList) {
			var ntc = (NBTTagCompound) tag;
			if (ntc.getInteger(countTag) % factor != 0) {
				return true;
			}
		}
		return false;
	}

	// Searches and replaces items. Duh :)
	private void searchAndReplace(IContainerPatternMultiTool host, EntityPlayerMP player) {
		var srInv = host.getSearchReplaceInventory();
		var inv = host.getPatternInventory();
		if (srInv == null || inv == null) return;

		var itemA = srInv.getStackInSlot(0);
		var itemB = srInv.getStackInSlot(1);
		if (itemA.isEmpty() || itemB.isEmpty()) return;

		var itemBData = ItemStackHelper.stackToNBT(itemB);
		var crafting = new InventoryCrafting(new ContainerNull(), 3, 3);

		for (var i = 0; i < inv.getSlots(); i++) {
			var is = inv.getStackInSlot(i);
			if (!(is.getItem() instanceof ItemEncodedPattern)) continue;
			var nbt = is.getTagCompound();
			if (nbt == null) {
				// Skip this item if it has no NBT data
				continue;
			}

			var ae2fc = Platform.isModLoaded("ae2fc") && is.getItem() instanceof ItemFluidEncodedPattern;
			final var countTag = ae2fc ? "Cnt" : "Count"; // ¯\_(ツ)_/¯

			final var tagIn = (NBTTagList) nbt.getTag("in").copy();
			final var tagOut = (NBTTagList) nbt.getTag("out").copy();

			var fluidStackIn = FluidUtil.getFluidContained(itemA);
			var fluidStackOut = FluidUtil.getFluidContained(itemB);
			var fluidReplacement = ae2fc && fluidStackIn != null && fluidStackOut != null;

			var lists = new NBTTagList[]{ tagIn, tagOut };
			for (var list : lists) {
				var idx = 0;
				for (var tag : list.copy()) {
					var compound = (NBTTagCompound) tag;
					var stack = ItemStackHelper.stackFromNBT(compound);
					if (itemA.isItemEqual(stack)) {
						var count = compound.getTag(countTag).copy();
						var data = itemBData.copy();
						data.setTag(countTag, count);
						list.set(idx, data);
					} else if (fluidReplacement && stack.getItem() instanceof ItemFluidDrop) {
						// ¯\_(ツ)_/¯
						var fluidStack = FakeItemRegister.getStack(stack);
						if (fluidStackIn.isFluidEqual(((FluidStack) fluidStack))) {
							var ifd = FakeFluids.packFluid2Drops(fluidStackOut);
							NBTTagCompound ifdCompound;
							if (ifd == null || (ifdCompound = ifd.getTagCompound()) == null) continue;

							var data = compound.copy();
							data.setTag("tag", ifdCompound);
							list.set(idx, data);
						}
					}
					idx++;
				}
			}

			// Validate
			if (nbt.getBoolean("crafting")) {
				try {
					if (tagIn.tagCount() != 9) {
						continue;
					}
					var w = player.world;

					crafting.clear();
					for (var j = 0; j < tagIn.tagCount(); j++) {
						var is1 = ItemStackHelper.stackFromNBT((NBTTagCompound) tagIn.get(j));
						crafting.setInventorySlotContents(j, is1);
					}

					if (null == CraftingManager.findMatchingRecipe(crafting, w)) {
						continue;
					}
				} catch (Exception e) {
					continue;
				}
			}

			nbt.setTag("in", tagIn);
			nbt.setTag("out", tagOut);

			// ¯\_(ツ)_/¯
			if (ae2fc) {
				nbt.setTag("Inputs", tagIn);
				nbt.setTag("Outputs", tagOut);
			}
		}
	}

	// Updates the count of a pattern based on the operation and factor
	private void updatePatternCount(ItemStack is, int factor, Operation operation) {
		var ae2fc = Platform.isModLoaded("ae2fc") && is.getItem() instanceof ItemFluidEncodedPattern;

		var nbt = is.getTagCompound();
		if (nbt == null) {
			// Skip this item if it has no NBT data
			return;
		}

		final var tagIn = (NBTTagList) nbt.getTag("in");
		final var tagOut = (NBTTagList) nbt.getTag("out");

		final var countTag = ae2fc ? "Cnt" : "Count"; // ¯\_(ツ)_/¯

		// If operation is DIVIDE, check if all counts are divisible by the factor
		if (operation == Operation.DIVIDE &&
			(this.canDivideTagList(tagIn, factor, countTag) || this.canDivideTagList(tagOut, factor, countTag))) {
			// If any count is not divisible by the factor, don't modify the pattern
			return;
		}

		var toModify = new ArrayList<NBTTagList>(4);

		// I don't know why AE2FC keeps a different set of tags.
		if (ae2fc) {
			final var ae2fcTagIn = (NBTTagList) nbt.getTag("Inputs");
			final var ae2fcTagOut = (NBTTagList) nbt.getTag("Outputs");
			if (operation == Operation.DIVIDE &&
				(this.canDivideTagList(ae2fcTagIn, factor, countTag) || this.canDivideTagList(ae2fcTagOut,
					factor,
					countTag))) {
				return;
			}

			toModify.add(ae2fcTagIn);
			toModify.add(ae2fcTagOut);
		}

		toModify.add(tagIn);
		toModify.add(tagOut);

		for (var list : toModify) {
			this.modifyTagList(list, factor, operation, countTag);
		}

		var newNbt = is.getTagCompound();
		newNbt.setByte("crafting", (byte) 0);
		newNbt.setByte("substitute", (byte) 0);
	}

	// Modifies the count of each tag in the list based on the operation and factor
	private void modifyTagList(NBTTagList tagList, int factor, Operation operation, String countTag) {
		for (var tag : tagList) {
			var ntc = (NBTTagCompound) tag;
			var count = ntc.getInteger(countTag);
			if (count == 0) {
				continue;
			}

			switch (operation) {
				case ADD -> {
					if (count < Integer.MAX_VALUE) {
						ntc.setInteger(countTag, count + factor);
					}
				}
				case SUBTRACT -> {
					if (count > 1) {
						ntc.setInteger(countTag, count - factor);
					}
				}
				case MULTIPLY -> {
					if (count > 0 && count <= Integer.MAX_VALUE / factor) {
						ntc.setInteger(countTag, count * factor);
					}
				}
				case DIVIDE -> {
					if (count >= factor) {
						ntc.setInteger(countTag, Math.max(1, count / factor));
					}
				}
			}
			if (count > 64) {
				ntc.setInteger("stackSize", ntc.getInteger(countTag));
			} else {
				ntc.removeTag("stackSize");
			}
		}
	}

	// Enum for possible operations
	public enum Operation {
		ADD, SUBTRACT, MULTIPLY, DIVIDE
	}
}
