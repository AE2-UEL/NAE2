package co.neeve.nae2.common.sync;

import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.client.gui.GuiNull;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.sync.GuiHostType;
import appeng.util.Platform;
import baubles.api.BaublesApi;
import co.neeve.nae2.NAE2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
	public void openGUI(@Nonnull final EntityPlayer p, @Nullable final TileEntity tile,
	                    @Nonnull final AEPartLocation side, @Nonnull final GuiBridge type) {
		if (Platform.isClient()) {
			return;
		}

		int x;
		var y = 0;
		var z = Integer.MIN_VALUE;

		if (tile != null) {
			x = tile.getPos().getX();
			y = tile.getPos().getY();
			z = tile.getPos().getZ();
		} else {
			if (p.openContainer instanceof IInventorySlotAware) {
				x = ((IInventorySlotAware) p.openContainer).getInventorySlot();
				y = ((IInventorySlotAware) p.openContainer).isBaubleSlot() ? 1 : 0;
			} else {
				x = p.inventory.currentItem;
			}
		}

		if ((type.getHostType().isItem() && tile == null) || type.hasPermissions(tile, x, y, z, side, p)) {
			if (tile == null && type.getHostType() == GuiHostType.ITEM) {
				p.openGui(NAE2.instance, type.ordinal() << 4, p.getEntityWorld(), x, 0, 0);
			} else if (tile == null || type.getHostType() == GuiHostType.ITEM) {
				if (tile != null) {
					p.openGui(NAE2.instance, type.ordinal() << 4 | side.ordinal() | (1 << 3), p.getEntityWorld(), x, y
						, z);
				} else {
					p.openGui(NAE2.instance, type.ordinal() << 4, p.getEntityWorld(), x, y, z);
				}
			} else {
				p.openGui(NAE2.instance, type.ordinal() << 4 | side.ordinal(), tile.getWorld(), x, y, z);
			}
		}
	}

	@Override
	public Object getServerGuiElement(final int ordinal, final EntityPlayer player, final World w, final int x,
	                                  final int y, final int z) {
		final var side = AEPartLocation.fromOrdinal(ordinal & 0x07);
		final var ID = GuiBridge.getByID(ordinal >> 4);
		if (ID != null) {
			final var usingItemOnTile = ((ordinal >> 3) & 1) == 1;
			if (ID.getHostType().isItem()) {
				final ItemStack it;
				final Object myItem;
				if (usingItemOnTile) {
					it = player.inventory.getCurrentItem();
					myItem = GuiBridge.getGuiObject(it, player, w, x, y, z, side);
				} else if (y == 0 && x >= 0 && x < player.inventory.mainInventory.size()) {
					it = player.inventory.getStackInSlot(x);
					myItem = GuiBridge.getGuiObject(it, w);
				} else if (y == 1 && z == Integer.MIN_VALUE) {
					it = BaublesApi.getBaublesHandler(player).getStackInSlot(x);
					myItem = GuiBridge.getGuiObject(it, w);
				} else {
					return new ContainerNull();
				}

				if (myItem != null && ID.CorrectTileOrPart(myItem)) {
					return this.updateGui(ID.ConstructContainer(player.inventory, myItem), w, x, y, z, side,
						myItem);
				}
			}
			if (!ID.getHostType().isItem()) {
				final var TE = w.getTileEntity(new BlockPos(x, y, z));
				if (TE instanceof IPartHost) {
					((IPartHost) TE).getPart(side);
					final var part = ((IPartHost) TE).getPart(side);
					if (ID.CorrectTileOrPart(part)) {
						return this.updateGui(ID.ConstructContainer(player.inventory, part), w, x, y, z, side,
							part);
					}
				} else {
					if (ID.CorrectTileOrPart(TE)) {
						return this.updateGui(ID.ConstructContainer(player.inventory, TE), w, x, y, z, side, TE);
					}
				}
			}
		}
		return new ContainerNull();
	}

	private Object updateGui(final Object newContainer, final World w, final int x, final int y, final int z,
	                         final AEPartLocation side, final Object myItem) {
		if (newContainer instanceof AEBaseContainer bc) {
			bc.setOpenContext(new ContainerOpenContext(myItem));
			bc.getOpenContext().setWorld(w);
			bc.getOpenContext().setX(x);
			bc.getOpenContext().setY(y);
			bc.getOpenContext().setZ(z);
			bc.getOpenContext().setSide(side);
		}

		return newContainer;
	}

	public Object getClientGuiElement(int ordinal, EntityPlayer player, World w, int x, int y, int z) {
		var side = AEPartLocation.fromOrdinal(ordinal & 7);
		var ID = GuiBridge.getByID(ordinal >> 4);
		if (ID != null) {
			var usingItemOnTile = (ordinal >> 3 & 1) == 1;
			if (ID.getHostType().isItem()) {
				final ItemStack it;
				final Object myItem;
				if (usingItemOnTile) {
					it = player.inventory.getCurrentItem();
					myItem = GuiBridge.getGuiObject(it, player, w, x, y, z, side);
				} else if (y == 0 && x >= 0 && x < player.inventory.mainInventory.size()) {
					it = player.inventory.getStackInSlot(x);
					myItem = GuiBridge.getGuiObject(it, w);
				} else if (y == 1 && z == Integer.MIN_VALUE) {
					it = BaublesApi.getBaublesHandler(player).getStackInSlot(x);
					myItem = GuiBridge.getGuiObject(it, w);
				} else {
					return new GuiNull(new ContainerNull());
				}

				if (myItem != null && ID.CorrectTileOrPart(myItem)) {
					return ID.ConstructGui(player.inventory, myItem);
				}
			}

			if (!ID.getHostType().isItem()) {
				var TE = w.getTileEntity(new BlockPos(x, y, z));
				if (TE instanceof IPartHost) {
					((IPartHost) TE).getPart(side);
					var part = ((IPartHost) TE).getPart(side);
					if (ID.CorrectTileOrPart(part)) {
						return ID.ConstructGui(player.inventory, part);
					}
				} else if (ID.CorrectTileOrPart(TE)) {
					return ID.ConstructGui(player.inventory, TE);
				}
			}
		}

		return new GuiNull(new ContainerNull());
	}
}
