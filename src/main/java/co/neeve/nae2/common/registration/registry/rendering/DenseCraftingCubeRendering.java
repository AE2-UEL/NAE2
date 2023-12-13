package co.neeve.nae2.common.registration.registry.rendering;

import appeng.block.crafting.BlockCraftingUnit;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.rendering.models.crafting.DenseCraftingCubeModel;
import co.neeve.nae2.common.blocks.BlockDenseCraftingUnit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class DenseCraftingCubeRendering extends BlockRenderingCustomizer {
	private final String registryName;
	private final BlockDenseCraftingUnit.DenseCraftingUnitType type;

	public DenseCraftingCubeRendering(String registryName, BlockDenseCraftingUnit.DenseCraftingUnitType type) {
		this.registryName = registryName;
		this.type = type;
	}

	@SideOnly(Side.CLIENT)
	public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
		var baseName = new ResourceLocation(Tags.MODID, this.registryName);
		var defaultModel = new ModelResourceLocation(baseName, "normal");
		var builtInName = "models/block/crafting/" + this.registryName + "/builtin";
		var builtInModelName = new ModelResourceLocation(new ResourceLocation(Tags.MODID, builtInName),
			"normal");
		rendering.builtInModel(builtInName, new DenseCraftingCubeModel(this.type));
		rendering.stateMapper((block) -> this.mapState(block, defaultModel, builtInModelName));
		rendering.modelCustomizer((loc, model) -> model);
	}

	@SideOnly(Side.CLIENT)
	private Map<IBlockState, ModelResourceLocation> mapState(Block block, ModelResourceLocation defaultModel,
	                                                         ModelResourceLocation formedModel) {
		Map<IBlockState, ModelResourceLocation> result = new HashMap<>();

		for (var state : block.getBlockState().getValidStates()) {
			if (state.getValue(BlockCraftingUnit.FORMED)) {
				result.put(state, formedModel);
			} else {
				result.put(state, defaultModel);
			}
		}

		return result;
	}
}
