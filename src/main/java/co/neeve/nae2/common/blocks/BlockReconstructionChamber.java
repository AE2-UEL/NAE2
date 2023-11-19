package co.neeve.nae2.common.blocks;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.sync.GuiBridge;
import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BlockReconstructionChamber extends AEBaseTileBlock {
	public BlockReconstructionChamber() {
		super(Material.GLASS);
		this.setOpaque(false);
		this.lightOpacity = 1;
	}

	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(@NotNull IBlockState state) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public @NotNull BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean onActivated(World w, BlockPos pos, EntityPlayer player, EnumHand hand,
	                           @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (this.getTileEntity(w, pos) instanceof TileReconstructionChamber trc && !player.isSneaking()) {
			NAE2.gui().openGUI(player, trc, AEPartLocation.fromFacing(side), GuiBridge.RECONSTRUCTION_CHAMBER);
			return true;
		}
		return false;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World w, BlockPos pos) {
		if (this.getTileEntity(w, pos) instanceof TileReconstructionChamber trc) {
			if (trc.getInputInvRaw().getSlots() > 0) {
				return ItemHandlerHelper.calcRedstoneFromInventory(trc.getInputInvRaw());
			}
		}

		return 0;
	}

	@Override
	public void onNeighborChange(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
		if (this.getTileEntity(world, pos) instanceof TileReconstructionChamber trc) {
			trc.updateNeighbors(world, pos, neighbor);
		}
	}
}
