package co.neeve.nae2.items.patternmultiplier;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.core.AppEng;
import appeng.helpers.IInterfaceHost;
import appeng.items.AEBaseItem;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ToolPatternMultiplier extends AEBaseItem implements IGuiItem {
    public ToolPatternMultiplier() {
        setRegistryName(Tags.MODID, "pattern_multiplier");
        setTranslationKey(Tags.MODID + ".pattern_multiplier");
        setMaxStackSize(1);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(final @NotNull World w, final @NotNull EntityPlayer p, final @NotNull EnumHand hand) {
        if (Platform.isServer()) {
            final RayTraceResult mop = AppEng.proxy.getRTR();

            if (mop == null || mop.typeOfHit == RayTraceResult.Type.MISS) {
                p.openGui(NAE2.instance, GuiHandlerPatternMultiplier.GuiIDs.PATTERN_MULTIPLIER.ordinal(), w, 0, 0, 0);
                p.swingArm(hand);
                return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, p.getHeldItem(hand));
    }

    public @NotNull EnumActionResult onItemUseFirst(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX, float hitY, float hitZ, @NotNull EnumHand hand) {
        // TODO: Send a packet to server instead. Amusingly, this prevents handling if clicking from y > 255.
        if (player.isSneaking() || !Platform.isServer()) {
            return EnumActionResult.PASS;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            player.openGui(NAE2.instance, GuiHandlerPatternMultiplier.GuiIDs.PATTERN_MULTIPLIER.ordinal(), world, 0, 0, 0);

            return EnumActionResult.SUCCESS;
        }

        IGridProxyable iface;

        // Check if we're poking a multipart, otherwise check if the TE is actually an Interface.
        int facing = 0;
        if (te instanceof IPartHost) {
            final RayTraceResult mop = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
            SelectedPart partHost = ((IPartHost) te).selectPart(mop.hitVec);

            if (partHost.part instanceof IInterfaceHost && partHost.part instanceof IGridProxyable ifaceHost) {
                iface = ifaceHost;
                // +1 to indicate the value is actually there, still between 0..7 (3 bits).
                facing = partHost.side.ordinal() + 1;
            } else {
                // Click through...
                return EnumActionResult.PASS;
            }
        } else if (te instanceof IInterfaceHost && te instanceof IGridProxyable ifaceTe) {
            iface = ifaceTe;
        } else {
            return EnumActionResult.PASS;
        }

        ISecurityGrid cache;
        try {
            cache = iface.getProxy().getSecurity();
        } catch (GridAccessException e) {
            // No grid access, or grid is not initialized.
            return EnumActionResult.FAIL;
        }

        // Check for network permissions.
        if (!cache.hasPermission(player, SecurityPermissions.EXTRACT)
                || !cache.hasPermission(player, SecurityPermissions.INJECT)) return EnumActionResult.SUCCESS;

        // Write additional data to the Y axis. We're accessing blocks, so anything past 0..255 is irrelevant.
        player.openGui(NAE2.instance, GuiHandlerPatternMultiplier.GuiIDs.PATTERN_MULTIPLIER_IFACE.ordinal(),
                player.getEntityWorld(), pos.getX(), (pos.getY() & 0xFF) | ((facing & 0x7) << 8), pos.getZ());

        return EnumActionResult.SUCCESS;
    }

    public IGuiItemObject getGuiObject(ItemStack is, World w, BlockPos bp) {
        IInterfaceHost iface = null;

        if (bp != null) {
            int yCoord = bp.getY() & 0xFF;
            int facing = ((bp.getY() >> 8) & 0x7);
            TileEntity te = w.getTileEntity(new BlockPos(bp.getX(), yCoord, bp.getZ()));

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

        return new ObjPatternMultiplier(is, iface);
    }
}
