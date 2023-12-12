package co.neeve.nae2.common.blocks;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.sync.GuiBridge;
import co.neeve.nae2.common.tiles.TilePuller;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPuller extends AEBaseTileBlock {
	public BlockPuller() {
		super(Material.IRON);
	}

	@Override
	public boolean onActivated(World w, BlockPos pos, EntityPlayer player, EnumHand hand,
	                           @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (this.getTileEntity(w, pos) instanceof TilePuller trc && !player.isSneaking()) {
			NAE2.gui().openGUI(player, trc, AEPartLocation.fromFacing(side), GuiBridge.PULLER);
			return true;
		}
		return false;
	}
}
