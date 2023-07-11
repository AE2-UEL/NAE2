package co.neeve.nae2.items.patternmultiplier;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.IGridProxyable;
import co.neeve.nae2.client.gui.implementations.GuiPatternMultiplier;
import co.neeve.nae2.common.containers.ContainerPatternMultiplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

public class GuiHandlerPatternMultiplier implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIDs.PATTERN_MULTIPLIER.ordinal() || ID == GuiIDs.PATTERN_MULTIPLIER_IFACE.ordinal()) {
            ItemStack heldItemMainhand = player.getHeldItemMainhand();
            ItemStack it = heldItemMainhand.getItem() instanceof ToolPatternMultiplier ? heldItemMainhand : player.getHeldItemOffhand();

            IInterfaceHost iface = getiInterfaceHost(world, x, y, z);

            if (it.getItem() instanceof ToolPatternMultiplier ipm) {
                return new ContainerPatternMultiplier(player.inventory,
                        (ObjPatternMultiplier) ipm.getGuiObject(it, null, null), iface);
            }
        }
        return null;
    }

    @Nullable
    private static IInterfaceHost getiInterfaceHost(World world, int x, int y, int z) {
        IInterfaceHost iface = null;
        BlockPos bp = new BlockPos(x, y, z);
        {
            int yCoord = bp.getY() & 0xFF;
            int facing = ((bp.getY() >> 8) & 0x7);
            TileEntity te = world.getTileEntity(new BlockPos(bp.getX(), yCoord, bp.getZ()));

            // This is a part host
            if (facing > 0 && te instanceof IPartHost partHost) {
                IPart part = partHost.getPart(EnumFacing.byIndex(facing - 1));
                if (part instanceof IGridProxyable && part instanceof IInterfaceHost ifacePart) {
                    iface = ifacePart;
                }
            } else if (facing == 0 && te instanceof IInterfaceHost ifaceHost) {
                iface = ifaceHost;
            }
        }
        return iface;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIDs.PATTERN_MULTIPLIER.ordinal() | ID == GuiIDs.PATTERN_MULTIPLIER_IFACE.ordinal()) {
            ItemStack heldItemMainhand = player.getHeldItemMainhand();
            ItemStack it = heldItemMainhand.getItem() instanceof ToolPatternMultiplier ? heldItemMainhand : player.getHeldItemOffhand();

            IInterfaceHost iface = getiInterfaceHost(world, x, y, z);

            if (it.getItem() instanceof ToolPatternMultiplier ipm) {
                return new GuiPatternMultiplier(player.inventory,
                        (ObjPatternMultiplier) ipm.getGuiObject(it, null, null), iface);
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
