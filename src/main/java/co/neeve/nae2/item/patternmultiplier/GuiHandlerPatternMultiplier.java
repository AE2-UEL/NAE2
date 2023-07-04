package co.neeve.nae2.item.patternmultiplier;

import co.neeve.nae2.item.patternmultiplier.client.GuiPatternMultiplier;
import co.neeve.nae2.item.patternmultiplier.container.ContainerPatternMultiplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandlerPatternMultiplier implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        ItemStack it = getPlayerItemStack(player, x);

        if (it.getItem() instanceof ItemPatternMultiplier ipm && ID == GuiIDs.PATTERN_MULTIPLIER.ordinal()) {
            return new ContainerPatternMultiplier(player.inventory, (ObjPatternMultiplier) ipm.getGuiObject(it, world, new BlockPos(x, y, z)));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        ItemStack it = getPlayerItemStack(player, x);

        if (it.getItem() instanceof ItemPatternMultiplier ipm && ID == GuiIDs.PATTERN_MULTIPLIER.ordinal()) {
            return new GuiPatternMultiplier(player.inventory, (ObjPatternMultiplier) ipm.getGuiObject(it, world, new BlockPos(x, y, z)));
        }
        return null;
    }

    // Helper method to get player's item stack
    private ItemStack getPlayerItemStack(EntityPlayer player, int x) {
        ItemStack it = ItemStack.EMPTY;
        if (x >= 0 && x < player.inventory.mainInventory.size()) {
            it = player.inventory.getStackInSlot(x);
        }
        return it;
    }

    // Enum for Gui IDs
    public enum GuiIDs {
        PATTERN_MULTIPLIER
    }
}
