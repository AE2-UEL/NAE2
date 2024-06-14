package co.neeve.nae2.common.registration.definitions;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.bootstrap.components.IRecipeRegistrationComponent;
import appeng.core.features.ItemDefinition;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.gui.PatternMultiToolButtonHandler;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.features.subfeatures.VoidCellFeatures;
import co.neeve.nae2.common.items.VirtualPattern;
import co.neeve.nae2.common.items.cells.DenseFluidCell;
import co.neeve.nae2.common.items.cells.DenseGasCell;
import co.neeve.nae2.common.items.cells.DenseItemCell;
import co.neeve.nae2.common.items.cells.handlers.VoidCellHandler;
import co.neeve.nae2.common.items.cells.vc.VoidFluidCell;
import co.neeve.nae2.common.items.cells.vc.VoidGasCell;
import co.neeve.nae2.common.items.cells.vc.VoidItemCell;
import co.neeve.nae2.common.items.patternmultitool.ToolPatternMultiTool;
import co.neeve.nae2.common.recipes.handlers.VoidConversionRecipe;
import co.neeve.nae2.common.registration.registry.Registry;
import co.neeve.nae2.common.registration.registry.interfaces.Definitions;
import co.neeve.nae2.common.registration.registry.rendering.NoItemRendering;
import com.mekeng.github.common.ItemAndBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Items implements Definitions<IItemDefinition> {
	private final Object2ObjectOpenHashMap<String, IItemDefinition> byId = new Object2ObjectOpenHashMap<>();

	private final IItemDefinition patternMultiTool;

	private final IItemDefinition storageCellVoid;
	private final IItemDefinition fluidStorageCellVoid;
	private final IItemDefinition gasStorageCellVoid;
	private final IItemDefinition storageCell256K;
	private final IItemDefinition storageCell1024K;
	private final IItemDefinition storageCell4096K;
	private final IItemDefinition storageCell16384K;
	private final IItemDefinition storageCellFluid256K;
	private final IItemDefinition storageCellFluid1024K;
	private final IItemDefinition storageCellFluid4096K;
	private final IItemDefinition storageCellFluid16384K;
	private final IItemDefinition storageCellGas256K;
	private final IItemDefinition storageCellGas1024K;
	private final IItemDefinition storageCellGas4096K;
	private final IItemDefinition storageCellGas16384K;
	private final IItemDefinition virtualPattern;

	public Items(Registry registry) {
		this.virtualPattern = this.registerById(registry.item("virtual_pattern", VirtualPattern::new)
			.hide()
			.rendering(new NoItemRendering())
			.build());

		this.patternMultiTool = this.registerById(registry.item("pattern_multiplier", ToolPatternMultiTool::new)
			.features(Features.PATTERN_MULTI_TOOL)
			.bootstrap((item) -> (IPostInitComponent) r -> {
				Upgrades.CAPACITY.registerItem(new ItemStack(item, 1), 3);

				if (r.isClient()) {
					MinecraftForge.EVENT_BUS.register(new PatternMultiToolButtonHandler());
				}
			})
			.build());

		// Void Cells.
		this.storageCellVoid = this.registerById(
			registry.item("storage_cell_void", VoidItemCell::new)
				.features(Features.VOID_CELLS)
				.build());

		this.fluidStorageCellVoid = this.registerById(
			registry.item("fluid_storage_cell_void", VoidFluidCell::new)
				.features(Features.VOID_CELLS)
				.build());

		this.gasStorageCellVoid = this.registerById(
			registry.item("gas_storage_cell_void", VoidGasCell::new)
				.features(Features.VOID_CELLS, Features.DENSE_GAS_CELLS)
				.build());

		var voidCells = new Object2ObjectArrayMap<String, IItemDefinition>();
		if (this.storageCellVoid.isEnabled()) voidCells.put("item", this.storageCellVoid);
		if (this.fluidStorageCellVoid.isEnabled()) voidCells.put("fluid", this.fluidStorageCellVoid);
		if (this.gasStorageCellVoid.isEnabled()) voidCells.put("gas", this.gasStorageCellVoid);

		registry.addBootstrapComponent((IPostInitComponent) r -> {
			if (!voidCells.isEmpty()) {
				AEApi.instance().registries().cell().addCellHandler(new VoidCellHandler());
			}
		});

		registry.addBootstrapComponent((IRecipeRegistrationComponent) (side, r) -> {
			if (!VoidCellFeatures.CONVERSION_RECIPES.isEnabled()) return;

			// Register circular conversion.
			if (voidCells.size() > 1) {
				var entrySet = voidCells.entrySet().stream().collect(Collectors.toList());
				for (var i = 0; i < entrySet.size(); i++) {
					var from = entrySet.get(i);
					var to = entrySet.get((i + 1) % voidCells.size());
					from.getValue().maybeStack(1).ifPresent(fromStack ->
						to.getValue().maybeStack(1).ifPresent(toStack ->
							r.register(new VoidConversionRecipe(fromStack, toStack)
								.setRegistryName(Tags.MODID, "void_conversion_" +
									from.getKey() + "_to_" + to.getKey()))));
				}
			}
		});

		this.storageCell256K = this.registerById(registry.item("storage_cell_256k", () ->
				new DenseItemCell(Materials.MaterialType.CELL_PART_256K,
					(int) Math.pow(2, 8)))
			.features(Features.DENSE_CELLS)
			.build());

		this.storageCell1024K = this.registerById(registry.item("storage_cell_1024k", () ->
				new DenseItemCell(Materials.MaterialType.CELL_PART_1024K,
					(int) Math.pow(2, 10)))
			.features(Features.DENSE_CELLS)
			.build());

		this.storageCell4096K = this.registerById(registry.item("storage_cell_4096k", () ->
				new DenseItemCell(Materials.MaterialType.CELL_PART_4096K,
					(int) Math.pow(2, 12)))
			.features(Features.DENSE_CELLS)
			.build());

		this.storageCell16384K = this.registerById(registry.item("storage_cell_16384k", () ->
				new DenseItemCell(Materials.MaterialType.CELL_PART_16384K,
					(int) Math.pow(2, 14)))
			.features(Features.DENSE_CELLS)
			.build());

		this.storageCellFluid256K = this.registerById(registry.item("storage_cell_fluid_256k", () ->
				new DenseFluidCell(Materials.MaterialType.CELL_FLUID_PART_256K,
					(int) Math.pow(2, 8)))
			.features(Features.DENSE_FLUID_CELLS)
			.build());

		this.storageCellFluid1024K = this.registerById(registry.item("storage_cell_fluid_1024k", () ->
				new DenseFluidCell(Materials.MaterialType.CELL_FLUID_PART_1024K,
					(int) Math.pow(2, 10)))
			.features(Features.DENSE_FLUID_CELLS)
			.build());

		this.storageCellFluid4096K = this.registerById(registry.item("storage_cell_fluid_4096k", () ->
				new DenseFluidCell(Materials.MaterialType.CELL_FLUID_PART_4096K,
					(int) Math.pow(2, 12)))
			.features(Features.DENSE_FLUID_CELLS)
			.build());

		this.storageCellFluid16384K = this.registerById(registry.item("storage_cell_fluid_16384k", () ->
				new DenseFluidCell(Materials.MaterialType.CELL_FLUID_PART_16384K,
					(int) Math.pow(2, 14)))
			.features(Features.DENSE_FLUID_CELLS)
			.build());

		this.storageCellGas256K = this.registerById(registry.item("storage_cell_gas_256k", () ->
				new DenseGasCell(Materials.MaterialType.CELL_GAS_PART_256K,
					(int) Math.pow(2, 8)))
			.features(Features.DENSE_GAS_CELLS)
			.build());

		this.storageCellGas1024K = this.registerById(registry.item("storage_cell_gas_1024k", () ->
				new DenseGasCell(Materials.MaterialType.CELL_GAS_PART_1024K,
					(int) Math.pow(2, 10)))
			.features(Features.DENSE_GAS_CELLS)
			.build());

		this.storageCellGas4096K = this.registerById(registry.item("storage_cell_gas_4096k", () ->
				new DenseGasCell(Materials.MaterialType.CELL_GAS_PART_4096K,
					(int) Math.pow(2, 12)))
			.features(Features.DENSE_GAS_CELLS)
			.build());

		this.storageCellGas16384K = this.registerById(registry.item("storage_cell_gas_16384k", () ->
				new DenseGasCell(Materials.MaterialType.CELL_GAS_PART_16384K,
					(int) Math.pow(2, 14)))
			.features(Features.DENSE_GAS_CELLS)
			.build());

		registry.addBootstrapComponent((IPostInitComponent) r -> {
			var items = AEApi.instance().definitions().items();
			var cellDef = items.cell1k();
			if (Features.DENSE_CELLS.isEnabled() && cellDef.isEnabled()) {
				mirrorCellUpgrades(cellDef, new IItemDefinition[]{
					this.storageCell256K,
					this.storageCell1024K,
					this.storageCell4096K,
					this.storageCell16384K,
					this.storageCellVoid
				});
			}

			var fluidCellDef = items.fluidCell1k();
			if (Features.DENSE_FLUID_CELLS.isEnabled() && fluidCellDef.isEnabled()) {
				mirrorCellUpgrades(fluidCellDef, new IItemDefinition[]{
					this.storageCellFluid256K,
					this.storageCellFluid1024K,
					this.storageCellFluid4096K,
					this.storageCellFluid16384K,
					this.fluidStorageCellVoid
				});
			}

			if (Features.DENSE_GAS_CELLS.isEnabled()) {
				mirrorCellUpgrades(new ItemStack(ItemAndBlocks.GAS_CELL_1k), new IItemDefinition[]{
					this.storageCellGas256K,
					this.storageCellGas1024K,
					this.storageCellGas4096K,
					this.storageCellGas16384K,
					this.storageCellVoid
				});
			}
		});
	}

	private static void mirrorCellUpgrades(Function<ItemStack, Boolean> predicate, IItemDefinition[] cells) {
		var supported = new java.util.HashMap<Upgrades, Integer>();
		Arrays.stream(Upgrades.values())
			.forEach(upgrade ->
				upgrade.getSupported().entrySet().stream()
					.filter(x -> predicate.apply(x.getKey()))
					.map(Map.Entry::getValue)
					.findFirst()
					.ifPresent(value -> supported.put(upgrade, value)));

		Arrays.stream(cells).forEach(iItemDefinition ->
			supported.forEach((key, value) ->
				key.registerItem(iItemDefinition, value)));
	}

	private static void mirrorCellUpgrades(IItemDefinition cellDef, IItemDefinition[] cells) {
		mirrorCellUpgrades(cellDef::isSameAs, cells);
	}

	private static void mirrorCellUpgrades(ItemStack itemStack, IItemDefinition[] cells) {
		mirrorCellUpgrades(itemStack::isItemEqual, cells);
	}

	private IItemDefinition registerById(ItemDefinition item) {
		this.byId.put(item.identifier(), item);
		return item;
	}

	@Override
	public Optional<IItemDefinition> getById(String id) {
		return Optional.ofNullable(this.byId.getOrDefault(id, null));
	}

	public IItemDefinition patternMultiTool() {
		return this.patternMultiTool;
	}

	public IItemDefinition storageCellVoid() {
		return this.storageCellVoid;
	}

	public IItemDefinition fluidStorageCellVoid() {
		return this.fluidStorageCellVoid;
	}

	public IItemDefinition gasStorageCellVoid() {
		return this.gasStorageCellVoid;
	}

	public IItemDefinition virtualPattern() {
		return this.virtualPattern;
	}

	public IItemDefinition storageCell256K() {
		return this.storageCell256K;
	}

	public IItemDefinition storageCell1024K() {
		return this.storageCell1024K;
	}

	public IItemDefinition storageCell4096K() {
		return this.storageCell4096K;
	}

	public IItemDefinition storageCell16384K() {
		return this.storageCell16384K;
	}

	public IItemDefinition storageCellFluid256K() {
		return this.storageCellFluid256K;
	}

	public IItemDefinition storageCellFluid1024K() {
		return this.storageCellFluid1024K;
	}

	public IItemDefinition storageCellFluid4096K() {
		return this.storageCellFluid4096K;
	}

	public IItemDefinition storageCellFluid16384K() {
		return this.storageCellFluid16384K;
	}

	public IItemDefinition storageCellGas256K() {
		return this.storageCellGas256K;
	}

	public IItemDefinition storageCellGas1024K() {
		return this.storageCellGas1024K;
	}

	public IItemDefinition storageCellGas4096K() {
		return this.storageCellGas4096K;
	}

	public IItemDefinition storageCellGas16384K() {
		return this.storageCellGas16384K;
	}
}
