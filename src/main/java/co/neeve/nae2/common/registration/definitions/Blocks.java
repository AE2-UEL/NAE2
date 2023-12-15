package co.neeve.nae2.common.registration.definitions;

import appeng.api.definitions.ITileDefinition;
import appeng.block.crafting.ItemCraftingStorage;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.components.IPreInitComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.util.Platform;
import co.neeve.nae2.client.rendering.tesr.TESRReconstructionChamber;
import co.neeve.nae2.common.blocks.BlockDenseCraftingUnit;
import co.neeve.nae2.common.blocks.BlockReconstructionChamber;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.features.subfeatures.DenseCellFeatures;
import co.neeve.nae2.common.integration.jei.NAEJEIPlugin;
import co.neeve.nae2.common.registration.registry.Registry;
import co.neeve.nae2.common.registration.registry.rendering.DenseCraftingCubeRendering;
import co.neeve.nae2.common.tiles.TileDenseCraftingUnit;
import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import de.ellpeck.actuallyadditions.mod.jei.reconstructor.ReconstructorRecipeCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
public class Blocks {
	private final ITileDefinition reconstructionChamber;
	private final ITileDefinition storageCrafting256K;
	private final ITileDefinition storageCrafting1024K;
	private final ITileDefinition storageCrafting4096K;
	private final ITileDefinition storageCrafting16384K;
	private final ITileDefinition coprocessor4x;
	private final ITileDefinition coprocessor16x;
	private final ITileDefinition coprocessor64x;

	public Blocks(Registry registry) {
		this.reconstructionChamber = registry.block("reconstruction_chamber",
				BlockReconstructionChamber::new)
			.tileEntity(new TileEntityDefinition(TileReconstructionChamber.class))
			.rendering(new BlockRenderingCustomizer() {
				@Override
				@SideOnly(Side.CLIENT)
				public void customize(IBlockRendering iBlockRendering, IItemRendering iItemRendering) {
					iBlockRendering.tesr(new TESRReconstructionChamber());
				}
			})
			.withJEIDescription()
			.bootstrap((block, item) -> (IPreInitComponent) side -> {
				if (side == Side.CLIENT && Platform.isModLoaded("jei")) {
					NAEJEIPlugin.registerCatalyst(this.reconstructionChamber(), ReconstructorRecipeCategory.NAME);
				}
			})
			.features(Features.RECONSTRUCTION_CHAMBER)
			.build();

		this.storageCrafting256K = registry.block("storage_crafting_256k", () -> new BlockDenseCraftingUnit(
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_256K))
			.tileEntity(new TileEntityDefinition(TileDenseCraftingUnit.class, "crafting_storage"))
			.rendering(new DenseCraftingCubeRendering("storage_crafting_256k",
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_256K))
			.useCustomItemModel()
			.item(ItemCraftingStorage::new)
			.features(DenseCellFeatures.DENSE_CPU_STORAGE_UNITS)
			.build();

		this.storageCrafting1024K = registry.block("storage_crafting_1024k", () -> new BlockDenseCraftingUnit(
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_1024K))

			.tileEntity(new TileEntityDefinition(TileDenseCraftingUnit.class, "crafting_storage"))
			.rendering(new DenseCraftingCubeRendering("storage_crafting_1024k",
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_1024K))
			.useCustomItemModel()
			.item(ItemCraftingStorage::new)
			.features(DenseCellFeatures.DENSE_CPU_STORAGE_UNITS)
			.build();

		this.storageCrafting4096K = registry.block("storage_crafting_4096k", () -> new BlockDenseCraftingUnit(
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_4096K))

			.tileEntity(new TileEntityDefinition(TileDenseCraftingUnit.class, "crafting_storage"))
			.rendering(new DenseCraftingCubeRendering("storage_crafting_4096k",
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_4096K))
			.useCustomItemModel()
			.item(ItemCraftingStorage::new)
			.features(DenseCellFeatures.DENSE_CPU_STORAGE_UNITS)
			.build();

		this.storageCrafting16384K = registry.block("storage_crafting_16384k", () -> new BlockDenseCraftingUnit(
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_16384K))

			.tileEntity(new TileEntityDefinition(TileDenseCraftingUnit.class, "crafting_storage"))
			.rendering(new DenseCraftingCubeRendering("storage_crafting_16384k",
				BlockDenseCraftingUnit.DenseCraftingUnitType.STORAGE_16384K))
			.useCustomItemModel()
			.item(ItemCraftingStorage::new)
			.features(DenseCellFeatures.DENSE_CPU_STORAGE_UNITS)
			.build();

		this.coprocessor4x = registry.block("coprocessor_4x", () -> new BlockDenseCraftingUnit(
				BlockDenseCraftingUnit.DenseCraftingUnitType.COPROCESSOR_4X))

			.tileEntity(new TileEntityDefinition(TileDenseCraftingUnit.class, "crafting_storage"))
			.rendering(new DenseCraftingCubeRendering("coprocessor_4x",
				BlockDenseCraftingUnit.DenseCraftingUnitType.COPROCESSOR_4X))
			.useCustomItemModel()
			.features(Features.DENSE_CPU_COPROCESSORS)
			.build();

		this.coprocessor16x = registry.block("coprocessor_16x", () -> new BlockDenseCraftingUnit(
				BlockDenseCraftingUnit.DenseCraftingUnitType.COPROCESSOR_16X))

			.tileEntity(new TileEntityDefinition(TileDenseCraftingUnit.class, "crafting_storage"))
			.rendering(new DenseCraftingCubeRendering("coprocessor_16x",
				BlockDenseCraftingUnit.DenseCraftingUnitType.COPROCESSOR_16X))
			.useCustomItemModel()
			.features(Features.DENSE_CPU_COPROCESSORS)
			.build();

		this.coprocessor64x = registry.block("coprocessor_64x", () -> new BlockDenseCraftingUnit(
				BlockDenseCraftingUnit.DenseCraftingUnitType.COPROCESSOR_64X))

			.tileEntity(new TileEntityDefinition(TileDenseCraftingUnit.class, "crafting_storage"))
			.rendering(new DenseCraftingCubeRendering("coprocessor_64x",
				BlockDenseCraftingUnit.DenseCraftingUnitType.COPROCESSOR_64X))
			.useCustomItemModel()
			.features(Features.DENSE_CPU_COPROCESSORS)
			.build();
	}

	public ITileDefinition reconstructionChamber() {
		return this.reconstructionChamber;
	}

	public ITileDefinition storageCrafting256K() {
		return this.storageCrafting256K;
	}

	public ITileDefinition storageCrafting1024K() {
		return this.storageCrafting1024K;
	}

	public ITileDefinition storageCrafting4096K() {
		return this.storageCrafting4096K;
	}

	public ITileDefinition storageCrafting16384K() {
		return this.storageCrafting16384K;
	}

	public ITileDefinition coprocessor4x() {
		return this.coprocessor4x;
	}

	public ITileDefinition coprocessor16x() {
		return this.coprocessor16x;
	}

	public ITileDefinition coprocessor64x() {
		return this.coprocessor64x;
	}
}
