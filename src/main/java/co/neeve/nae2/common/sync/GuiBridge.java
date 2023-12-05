package co.neeve.nae2.common.sync;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.AEBaseGui;
import appeng.container.AEBaseContainer;
import appeng.core.sync.GuiHostType;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import co.neeve.nae2.common.containers.ContainerPatternMultiTool;
import co.neeve.nae2.common.containers.ContainerReconstructionChamber;
import co.neeve.nae2.common.interfaces.INAEGuiItem;
import co.neeve.nae2.common.items.patternmultitool.ObjPatternMultiTool;
import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;

public enum GuiBridge {
	STUB(),
	PATTERN_MULTI_TOOL(ObjPatternMultiTool.class, ContainerPatternMultiTool.class, GuiHostType.ITEM,
		SecurityPermissions.BUILD),
	RECONSTRUCTION_CHAMBER(TileReconstructionChamber.class, ContainerReconstructionChamber.class, GuiHostType.WORLD,
		SecurityPermissions.BUILD);

	private static GuiBridge[] cachedValues;
	private final Class<?> clazz;
	private final Class<? extends AEBaseContainer> containerClass;
	private final GuiHostType hostType;
	private final SecurityPermissions securityPermissions;

	@SideOnly(Side.CLIENT)
	private Class<? super AEBaseGui> clientGuiClass;

	GuiBridge(Class<?> clazz, Class<? extends AEBaseContainer> containerClass, GuiHostType hostType,
	          SecurityPermissions securityPermissions) {
		this.hostType = hostType;
		this.securityPermissions = securityPermissions;
		if (IGuiItemObject.class.isAssignableFrom(clazz) || AEBaseTile.class.isAssignableFrom(clazz)) {
			this.clazz = clazz;
		} else {
			throw new IllegalArgumentException("Invalid GuiBridge class");
		}
		this.containerClass = containerClass;

		this.getGui();
	}

	GuiBridge() {
		this.clazz = null;
		this.containerClass = null;
		this.hostType = null;
		this.securityPermissions = null;
	}

	private static GuiBridge[] cachedValues() {
		if (cachedValues == null) {
			return cachedValues = values();
		}
		return cachedValues;
	}

	@Nullable
	public static GuiBridge getByID(int id) {
		if (id < 0 || id > cachedValues().length) return null;

		return cachedValues[id];
	}

	static Object getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y
		, final int z, AEPartLocation side) {
		if (!it.isEmpty()) {
			if (it.getItem() instanceof INAEGuiItem<?> ngi) {
				return ngi.getGuiObject(it, w, new BlockPos(x, y, z), side);
			}

			final var wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
			if (wh != null) {
				return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
			}
		}

		return null;
	}

	static Object getGuiObject(final ItemStack it, final World w) {
		if (!it.isEmpty()) {
			if (it.getItem() instanceof INAEGuiItem<?> ngi) {
				return ngi.getGuiObject(it, w);
			}
		}

		return null;
	}

	public boolean CorrectTileOrPart(final Object tE) {
		if (this.clazz == null) {
			throw new IllegalArgumentException("This Gui Cannot use the standard Handler.");
		}

		return this.clazz.isInstance(tE);
	}

	@SuppressWarnings("deprecation")
	private void getGui() {
		if (Platform.isClientInstall()) {
			//noinspection ResultOfMethodCallIgnored
			AEBaseGui.class.getName();
			var start = this.containerClass.getName();
			var guiClass = start.replaceFirst("common.containers.Container", "client.gui.implementations.Gui");
			if (start.equals(guiClass)) {
				throw new IllegalStateException("Unable to find gui class");
			}

			this.clientGuiClass = ReflectionHelper.getClass(this.getClass().getClassLoader(), guiClass);
		}
	}

	private Constructor<?> findConstructor(Constructor<?>[] constructors, InventoryPlayer inventory, Object tE) {
		for (var con : constructors) {
			var types = con.getParameterTypes();
			if (types.length == 2 && types[0].isAssignableFrom(inventory.getClass()) && types[1].isAssignableFrom(tE.getClass())) {
				return con;
			}
		}

		return null;
	}

	private String typeName(Object inventory) {
		return inventory == null ? "NULL" : inventory.getClass().getName();
	}

	public Object ConstructContainer(final InventoryPlayer inventory, final Object tE) {
		try {
			final var c = this.containerClass.getConstructors();
			if (c.length == 0) {
				throw new AppEngException("Invalid Gui Class");
			}

			final var target = this.findConstructor(c, inventory, tE);

			if (target == null) {
				throw new IllegalStateException("Cannot find " + this.containerClass.getName() + "( " + this.typeName(
					inventory) + ", " + this
					.typeName(tE) + " )");
			}

			return target.newInstance(inventory, tE);
		} catch (final Throwable t) {
			throw new IllegalStateException(t);
		}
	}

	public GuiHostType getHostType() {
		return this.hostType;
	}

	public Object ConstructGui(final InventoryPlayer inventory, final Object tE) {
		try {
			final var c = this.clientGuiClass.getConstructors();
			if (c.length == 0) {
				throw new AppEngException("Invalid Gui Class");
			}

			final var target = this.findConstructor(c, inventory, tE);

			if (target == null) {
				throw new IllegalStateException("Cannot find " + this.containerClass.getName() + "( " + this.typeName(
					inventory) + ", " + this
					.typeName(tE) + " )");
			}

			return target.newInstance(inventory, tE);
		} catch (final Throwable t) {
			throw new IllegalStateException(t);
		}
	}

	public boolean hasPermissions(final TileEntity te, final int x, final int y, final int z,
	                              final AEPartLocation side, final EntityPlayer player) {
		final var w = player.getEntityWorld();
		final var pos = new BlockPos(x, y, z);

		if (Platform.hasPermissions(te != null ? new DimensionalCoord(te) : new DimensionalCoord(player.world, pos),
			player)) {
			if (this.hostType.isItem()) {
				final var it = player.inventory.getCurrentItem();
				if (!it.isEmpty() && it.getItem() instanceof INAEGuiItem) {
					final Object myItem = ((INAEGuiItem<?>) it.getItem()).getGuiObject(it, w, pos, side);
					if (this.CorrectTileOrPart(myItem)) {
						return true;
					}
				}
			}

			if (!this.hostType.isItem()) {
				final var TE = w.getTileEntity(pos);
				if (TE instanceof IPartHost) {
					((IPartHost) TE).getPart(side);
					final var part = ((IPartHost) TE).getPart(side);
					if (this.CorrectTileOrPart(part)) {
						return this.securityCheck(part, player);
					}
				} else {
					if (this.CorrectTileOrPart(TE)) {
						return this.securityCheck(TE, player);
					}
				}
			}
		}
		return false;
	}

	private boolean securityCheck(final Object te, final EntityPlayer player) {
		if (te instanceof IActionHost && this.securityPermissions != null) {

			final var gn = ((IActionHost) te).getActionableNode();
			final var g = gn.getGrid();

			final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
			return sg.hasPermission(player, this.securityPermissions);

		}
		return true;
	}

}
