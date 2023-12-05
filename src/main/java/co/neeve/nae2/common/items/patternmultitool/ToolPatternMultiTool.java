package co.neeve.nae2.common.items.patternmultitool;

import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.helpers.IInterfaceHost;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.interfaces.INAEGuiItem;
import co.neeve.nae2.common.sync.GuiBridge;
import com.github.bsideup.jabel.Desugar;
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
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ToolPatternMultiTool extends AEBaseItem implements INAEGuiItem<ObjPatternMultiTool>, IBauble {
	public ToolPatternMultiTool() {
		this.setMaxStackSize(1);
	}

	public static ObjPatternMultiTool getGuiObject(ItemStack patternMultiTool) {
		return ((ToolPatternMultiTool) patternMultiTool.getItem()).getGuiObject(patternMultiTool, null);
	}

	@Nullable
	public static IInterfaceHost tryGetInterfacePart(TileEntity te, EnumFacing facing) {
		if (te instanceof IInterfaceHost ifh) return ifh;

		if (te instanceof IPartHost ph) {
			var part = ph.getPart(facing);
			if (part instanceof IInterfaceHost ifaceHost) {
				return ifaceHost;
			}
		}
		return null;
	}

	@Nullable
	public static InterfacePartResult tryGetInterfacePart(TileEntity te, BlockPos pos, EnumFacing side, float hitX,
	                                                      float hitY,
	                                                      float hitZ) {
		if (te instanceof IPartHost) {
			final var mop = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
			var selectedPart = ((IPartHost) te).selectPart(mop.hitVec);

			if (selectedPart.part instanceof IInterfaceHost interfaceHost) {
				return new InterfacePartResult(interfaceHost, selectedPart.side);
			}
		}
		return null;
	}

	@Override
	public @NotNull ActionResult<ItemStack> onItemRightClick(final @NotNull World w, final @NotNull EntityPlayer p,
	                                                         final @NotNull EnumHand hand) {
		if (Platform.isServer()) {
			final var mop = AppEng.proxy.getRTR();

			if (mop == null || mop.typeOfHit == RayTraceResult.Type.MISS) {
				NAE2.gui().openGUI(p, null, AEPartLocation.INTERNAL, GuiBridge.PATTERN_MULTI_TOOL);
				p.swingArm(hand);
				return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
			}
		}

		return new ActionResult<>(EnumActionResult.PASS, p.getHeldItem(hand));
	}

	public @NotNull EnumActionResult onItemUseFirst(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
	                                                @NotNull EnumFacing side, float hitX, float hitY, float hitZ,
	                                                @NotNull EnumHand hand) {
		// TODO: Send a packet to server instead. Amusingly, this prevents handling if clicking from y > 255.
		if (player.isSneaking() || !Platform.isServer()) {
			return EnumActionResult.PASS;
		}

		var te = world.getTileEntity(pos);
		if (te == null) {
			NAE2.gui().openGUI(player, null, AEPartLocation.INTERNAL, GuiBridge.PATTERN_MULTI_TOOL);

			return EnumActionResult.SUCCESS;
		} else if (te instanceof IInterfaceHost) {
			NAE2.gui().openGUI(player, te, AEPartLocation.INTERNAL, GuiBridge.PATTERN_MULTI_TOOL);
			return EnumActionResult.SUCCESS;
		}

		var partResult = tryGetInterfacePart(te, pos, side, hitX, hitY, hitZ);
		if (partResult != null) {
			NAE2.gui().openGUI(player, te, partResult.side, GuiBridge.PATTERN_MULTI_TOOL);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	@Override
	public ObjPatternMultiTool getGuiObject(ItemStack is, World w, BlockPos bp, AEPartLocation side) {
		IInterfaceHost interfaceHost = null;

		var te = w.getTileEntity(bp);
		if (te instanceof IInterfaceHost ih) {
			interfaceHost = ih;
		} else {
			var result = tryGetInterfacePart(te, side.getFacing());
			if (result != null) {
				interfaceHost = result;
			}
		}

		if (interfaceHost != null) {
			var obj = new ObjPatternMultiTool(is);
			obj.setInterface(interfaceHost);
			return obj;
		}

		return INAEGuiItem.super.getGuiObject(is, w, bp, side);
	}

	@Override
	@Optional.Method(modid = "baubles")
	public BaubleType getBaubleType(ItemStack itemStack) {
		return BaubleType.TRINKET;
	}

	@Override
	public ObjPatternMultiTool getGuiObject(ItemStack is, World w) {
		return new ObjPatternMultiTool(is);
	}

	@Desugar
	private record InterfacePartResult(IInterfaceHost part, AEPartLocation side) {}
}
