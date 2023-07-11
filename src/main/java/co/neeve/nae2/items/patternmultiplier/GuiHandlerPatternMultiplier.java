package co.neeve.nae2.items.patternmultiplier;

import co.neeve.nae2.client.gui.implementations.GuiPatternMultiplier;
import co.neeve.nae2.common.containers.ContainerPatternMultiplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandlerPatternMultiplier implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIDs.PATTERN_MULTIPLIER.ordinal() || ID == GuiIDs.PATTERN_MULTIPLIER_IFACE.ordinal()) {
            ItemStack heldItemMainhand = player.getHeldItemMainhand();
            ItemStack it = heldItemMainhand.getItem() instanceof ToolPatternMultiplier ? heldItemMainhand : player.getHeldItemOffhand();

            if (it.getItem() instanceof ToolPatternMultiplier ipm) {
                return new ContainerPatternMultiplier(player.inventory,
                        (ObjPatternMultiplier) ipm.getGuiObject(it, world, ID == GuiIDs.PATTERN_MULTIPLIER_IFACE.ordinal() ? new BlockPos(x, y, z) : null));
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIDs.PATTERN_MULTIPLIER.ordinal() | ID == GuiIDs.PATTERN_MULTIPLIER_IFACE.ordinal()) {
            ItemStack heldItemMainhand = player.getHeldItemMainhand();
            ItemStack it = heldItemMainhand.getItem() instanceof ToolPatternMultiplier ? heldItemMainhand : player.getHeldItemOffhand();

            if (it.getItem() instanceof ToolPatternMultiplier ipm) {
                return new GuiPatternMultiplier(player.inventory,
                        (ObjPatternMultiplier) ipm.getGuiObject(it, world, ID == GuiIDs.PATTERN_MULTIPLIER_IFACE.ordinal() ? new BlockPos(x, y, z) : null));
            }
        }
        return null;
    }

    // Enum for Gui IDs
    public enum GuiIDs {
        PATTERN_MULTIPLIER,
        PATTERN_MULTIPLIER_IFACE
    }
}
