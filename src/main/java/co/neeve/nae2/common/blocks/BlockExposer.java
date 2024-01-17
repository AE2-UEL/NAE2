package co.neeve.nae2.common.blocks;

import appeng.block.AEBaseTileBlock;
import co.neeve.nae2.NAE2;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockExposer extends AEBaseTileBlock {
	public BlockExposer() {
		super(Material.IRON);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
		super.addInformation(is, world, lines, advancedItemTooltips);

		NAE2.api().exposer().addTooltipInformation(is, world, lines, advancedItemTooltips);
	}
}
