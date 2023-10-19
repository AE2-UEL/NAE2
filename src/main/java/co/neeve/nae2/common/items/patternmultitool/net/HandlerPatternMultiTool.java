package co.neeve.nae2.common.items.patternmultitool.net;

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
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

public class HandlerPatternMultiTool implements IMessageHandler<PatternMultiToolPacket, IMessage> {

	// Handles incoming messages from PatternMultiToolPacket
	public IMessage onMessage(PatternMultiToolPacket message, MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		player.getServerWorld().addScheduledTask(() -> processMessage(message, player));
		return null;
	}

	// Processes a received message
	private void processMessage(PatternMultiToolPacket message, EntityPlayerMP player) {
		if (Platform.isClient()) return;

		if (player.openContainer instanceof AEBaseContainer bc && bc instanceof IPatternMultiToolHost container) {
			if (message.getActionType() == PatternMultiToolActionTypes.TAB_SWITCH && container instanceof ContainerPatternMultiTool containerPatternMultiTool) {
				containerPatternMultiTool.switchTab(PatternMultiToolTabs.values()[message.getValue()]);
			} else if (message.getActionType() == PatternMultiToolActionTypes.BUTTON_PRESS) {
				AppEngInternalInventory inv = (AppEngInternalInventory) container.getPatternMultiToolInventory();
				if (inv == null) return;

				PatternMultiToolActions value = PatternMultiToolActions.values()[message.getValue()];

				if (value == PatternMultiToolActions.INV_SWITCH && container instanceof IContainerPatternMultiTool cmp) {
					cmp.toggleInventory();
					bc.detectAndSendChanges();
					return;
				}

				if (value == PatternMultiToolActions.REPLACE && container instanceof IContainerPatternMultiTool cmp) {
					searchAndReplace(cmp, player);
					bc.detectAndSendChanges();
					return;
				}

				for (ItemStack is : inv) {
					if (is.getItem() instanceof ItemEncodedPattern) {
						try {
							handleButtonPress(is, bc, value);
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
			case MUL2 -> updatePatternCount(is, 2, Operation.MULTIPLY);
			case MUL3 -> updatePatternCount(is, 3, Operation.MULTIPLY);
			case ADD -> updatePatternCount(is, 1, Operation.ADD);
			case DIV2 -> updatePatternCount(is, 2, Operation.DIVIDE);
			case DIV3 -> updatePatternCount(is, 3, Operation.DIVIDE);
			case SUB -> updatePatternCount(is, 1, Operation.SUBTRACT);
			case CLEAR -> emptyPattern(is, container);
		}
	}

	private void emptyPattern(ItemStack is, AEBaseContainer container) {
		ItemStack newStack =
			AEApi.instance().definitions().materials().blankPattern().maybeStack(is.getCount()).orElse(ItemStack.EMPTY);

		container.putStackInSlot(container.getInventory().indexOf(is), newStack);
	}

	// Checks if each tag in the list can be divided evenly by the factor
	private boolean canDivideTagList(NBTTagList tagList, int factor, String countTag) {
		for (NBTBase tag : tagList) {
			NBTTagCompound ntc = (NBTTagCompound) tag;
			if (ntc.getInteger(countTag) % factor != 0) {
				return true;
			}
		}
		return false;
	}

	// Searches and replaces items. Duh :)
	private void searchAndReplace(IContainerPatternMultiTool host, EntityPlayerMP player) {
		var srInv = host.getSearchReplaceInventory();
		var inv = host.getPatternMultiToolInventory();
		if (srInv == null || inv == null) return;

		var itemA = srInv.getStackInSlot(0);
		var itemB = srInv.getStackInSlot(1);
		if (itemA.isEmpty() || itemB.isEmpty()) return;

		var itemBData = ItemStackHelper.stackToNBT(itemB);
		var crafting = new InventoryCrafting(new ContainerNull(), 3, 3);

		for (var i = 0; i < inv.getSlots(); i++) {
			var is = inv.getStackInSlot(i);
			if (!(is.getItem() instanceof ItemEncodedPattern)) continue;
			NBTTagCompound nbt = is.getTagCompound();
			if (nbt == null) {
				// Skip this item if it has no NBT data
				continue;
			}

			var ae2fc = Platform.isModLoaded("ae2fc") && is.getItem() instanceof ItemFluidEncodedPattern;
			final String countTag = ae2fc ? "Cnt" : "Count"; // ¯\_(ツ)_/¯

			final NBTTagList tagIn = (NBTTagList) nbt.getTag("in").copy();
			final NBTTagList tagOut = (NBTTagList) nbt.getTag("out").copy();

			var fluidStackIn = FluidUtil.getFluidContained(itemA);
			var fluidStackOut = FluidUtil.getFluidContained(itemB);
			var fluidReplacement = ae2fc && fluidStackIn != null && fluidStackOut != null;

			var lists = new NBTTagList[]{ tagIn, tagOut };
			for (var list : lists) {
				var idx = 0;
				for (NBTBase tag : list.copy()) {
					NBTTagCompound compound = (NBTTagCompound) tag;
					var stack = ItemStackHelper.stackFromNBT(compound);
					if (itemA.isItemEqual(stack)) {
						var count = compound.getTag(countTag).copy();
						var data = itemBData.copy();
						data.setTag(countTag, count);
						list.set(idx, data);
					} else if (fluidReplacement && stack.getItem() instanceof ItemFluidDrop) {
						// ¯\_(ツ)_/¯
						var fluidStack = ItemFluidDrop.getFluidStack(stack);
						if (fluidStackIn.isFluidEqual(fluidStack)) {
							var ifd = ItemFluidDrop.newStack(fluidStackOut);
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

		NBTTagCompound nbt = is.getTagCompound();
		if (nbt == null) {
			// Skip this item if it has no NBT data
			return;
		}

		final NBTTagList tagIn = (NBTTagList) nbt.getTag("in");
		final NBTTagList tagOut = (NBTTagList) nbt.getTag("out");

		final String countTag = ae2fc ? "Cnt" : "Count"; // ¯\_(ツ)_/¯

		// If operation is DIVIDE, check if all counts are divisible by the factor
		if (operation == Operation.DIVIDE &&
			(canDivideTagList(tagIn, factor, countTag) || canDivideTagList(tagOut, factor, countTag))) {
			// If any count is not divisible by the factor, don't modify the pattern
			return;
		}

		var toModify = new ArrayList<NBTTagList>(4);

		// I don't know why AE2FC keeps a different set of tags.
		if (ae2fc) {
			final NBTTagList ae2fcTagIn = (NBTTagList) nbt.getTag("Inputs");
			final NBTTagList ae2fcTagOut = (NBTTagList) nbt.getTag("Outputs");
			if (operation == Operation.DIVIDE &&
				(canDivideTagList(ae2fcTagIn, factor, countTag) || canDivideTagList(ae2fcTagOut, factor, countTag))) {
				return;
			}

			toModify.add(ae2fcTagIn);
			toModify.add(ae2fcTagOut);
		}

		toModify.add(tagIn);
		toModify.add(tagOut);

		for (var list : toModify) {
			modifyTagList(list, factor, operation, countTag);
		}

		NBTTagCompound newNbt = is.getTagCompound();
		newNbt.setByte("crafting", (byte) 0);
		newNbt.setByte("substitute", (byte) 0);
	}

	// Modifies the count of each tag in the list based on the operation and factor
	private void modifyTagList(NBTTagList tagList, int factor, Operation operation, String countTag) {
		for (NBTBase tag : tagList) {
			NBTTagCompound ntc = (NBTTagCompound) tag;
			int count = ntc.getInteger(countTag);
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
