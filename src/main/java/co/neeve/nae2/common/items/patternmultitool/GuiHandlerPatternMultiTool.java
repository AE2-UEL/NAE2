package co.neeve.nae2.common.items.patternmultitool;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.IGridProxyable;
import co.neeve.nae2.client.gui.implementations.GuiPatternMultiTool;
import co.neeve.nae2.common.containers.ContainerPatternMultiTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

public class GuiHandlerPatternMultiTool implements IGuiHandler {
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
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiIDs.PATTERN_MULTI_TOOL.ordinal() || ID == GuiIDs.PATTERN_MULTI_TOOL_IFACE.ordinal()) {
			ItemStack heldItemMainhand = player.getHeldItemMainhand();
			ItemStack it = heldItemMainhand.getItem() instanceof ToolPatternMultiTool ? heldItemMainhand :
				player.getHeldItemOffhand();

			IInterfaceHost iface = null;
			if (ID == GuiIDs.PATTERN_MULTI_TOOL_IFACE.ordinal()) {
				iface = getiInterfaceHost(world, x, y, z);
			}

			if (it.getItem() instanceof ToolPatternMultiTool ipm) {
				return new ContainerPatternMultiTool(player.inventory, ipm.getGuiObject(it, null, null), iface);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiIDs.PATTERN_MULTI_TOOL.ordinal() | ID == GuiIDs.PATTERN_MULTI_TOOL_IFACE.ordinal()) {
			ItemStack heldItemMainhand = player.getHeldItemMainhand();
			ItemStack it = heldItemMainhand.getItem() instanceof ToolPatternMultiTool ? heldItemMainhand :
				player.getHeldItemOffhand();

			IInterfaceHost iface = null;
			if (ID == GuiIDs.PATTERN_MULTI_TOOL_IFACE.ordinal()) {
				iface = getiInterfaceHost(world, x, y, z);
			}

			if (it.getItem() instanceof ToolPatternMultiTool ipm) {
				return new GuiPatternMultiTool(player.inventory, ipm.getGuiObject(it, null, null), iface);
			}
		}
		return null;
	}

	// Enum for Gui IDs
	public enum GuiIDs {
		PATTERN_MULTI_TOOL, PATTERN_MULTI_TOOL_IFACE
	}
}
