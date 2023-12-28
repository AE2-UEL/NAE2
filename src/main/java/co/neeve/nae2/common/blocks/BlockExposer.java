package co.neeve.nae2.common.blocks;

import appeng.block.AEBaseTileBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockExposer extends AEBaseTileBlock {
	public BlockExposer() {
		super(Material.IRON);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos,
	                            @NotNull Block blockIn, @NotNull BlockPos fromPos) {
		// if (world.getTileEntity(pos) instanceof TileExposer tileExposer) {
		//	tileExposer.onNeighborChange();
		//}
	}
}
