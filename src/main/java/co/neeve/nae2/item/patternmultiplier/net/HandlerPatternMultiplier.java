package co.neeve.nae2.item.patternmultiplier.net;

import appeng.api.AEApi;
import appeng.items.misc.ItemEncodedPattern;
import co.neeve.nae2.item.patternmultiplier.container.ContainerPatternMultiplier;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerPatternMultiplier implements IMessageHandler<PatternMultiplierPacket, IMessage> {

    // Handles incoming messages from PatternMultiplierPacket
    public IMessage onMessage(PatternMultiplierPacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> processMessage(message, player));
        return null;
    }

    // Processes a received message
    private void processMessage(PatternMultiplierPacket message, EntityPlayerMP player) {
        if (player.openContainer instanceof ContainerPatternMultiplier container) {
            NonNullList<ItemStack> inv = container.getInventory();
            for (ItemStack is : inv) {
                if (is.getItem() instanceof ItemEncodedPattern) {
                    handleButtonPress(is, container, message.getButtonId());
                }
            }
            container.detectAndSendChanges();
        }
    }

    // Handles the logic for when a button is pressed
    private void handleButtonPress(ItemStack is, ContainerPatternMultiplier container, int buttonId) {
        switch (buttonId) {
            case 0 -> updatePatternCount(is, 2, Operation.MULTIPLY);
            case 1 -> updatePatternCount(is, 3, Operation.MULTIPLY);
            case 2 -> updatePatternCount(is, 1, Operation.ADD);
            case 3 -> updatePatternCount(is, 2, Operation.DIVIDE);
            case 4 -> updatePatternCount(is, 3, Operation.DIVIDE);
            case 5 -> updatePatternCount(is, 1, Operation.SUBTRACT);
            case 6 -> emptyPattern(is, container);
        }
    }

    private void emptyPattern(ItemStack is, ContainerPatternMultiplier container) {
        ItemStack newStack = AEApi.instance().definitions().materials().blankPattern().maybeStack(is.getCount()).orElse(ItemStack.EMPTY);

        container.putStackInSlot(container.getInventory().indexOf(is), newStack);
    }

    // Checks if each tag in the list can be divided evenly by the factor
    private boolean canDivideTagList(NBTTagList tagList, int factor) {
        for (NBTBase tag : tagList) {
            NBTTagCompound ntc = (NBTTagCompound) tag;
            if (ntc.getInteger("Count") % factor != 0) {
                return true;
            }
        }
        return false;
    }

    // Updates the count of a pattern based on the operation and factor
    private void updatePatternCount(ItemStack is, int factor, Operation operation) {
        NBTTagCompound nbt = is.getTagCompound();
        if (nbt == null) {
            // Skip this item if it has no NBT data
            return;
        }

        final NBTTagList tagIn = (NBTTagList) nbt.getTag("in");
        final NBTTagList tagOut = (NBTTagList) nbt.getTag("out");

        // If operation is DIVIDE, check if all counts are divisible by the factor
        if (operation == Operation.DIVIDE && (canDivideTagList(tagIn, factor) || canDivideTagList(tagOut, factor))) {
            // If any count is not divisible by the factor, don't modify the pattern
            return;
        }

        NBTTagList newTagIn = modifyTagList(tagIn, factor, operation);
        NBTTagList newTagOut = modifyTagList(tagOut, factor, operation);

        NBTTagCompound newNbt = is.getTagCompound();
        newNbt.setTag("in", newTagIn);
        newNbt.setTag("out", newTagOut);
        newNbt.setByte("crafting", (byte)0);
        newNbt.setByte("substitute", (byte)0);
    }

    // Modifies the count of each tag in the list based on the operation and factor
    private NBTTagList modifyTagList(NBTTagList tagList, int factor, Operation operation) {
        NBTTagList newTagList = new NBTTagList();
        for (NBTBase tag : tagList) {
            NBTTagCompound ntc = (NBTTagCompound) tag;
            int count = ntc.getInteger("Count");
            if (count == 0) {
                continue;
            }

            switch (operation) {
                case ADD -> {
                    if (count < Integer.MAX_VALUE) {
                        ntc.setInteger("Count", count + factor);
                    }
                }
                case SUBTRACT -> {
                    if (count > 1) {
                        ntc.setInteger("Count", count - factor);
                    }
                }
                case MULTIPLY -> {
                    if (count > 0 && count <= Integer.MAX_VALUE / factor) {
                        ntc.setInteger("Count", count * factor);
                    }
                }
                case DIVIDE -> {
                    if (count >= factor) {
                        ntc.setInteger("Count", Math.max(1, count / factor));
                    }
                }
            }
            if (count > 64) {
                ntc.setInteger("stackSize", ntc.getInteger("Count"));
            } else {
                ntc.removeTag("stackSize");
            }
            newTagList.appendTag(ntc);
        }
        return newTagList;
    }

    // Enum for possible operations
    public enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }
}
