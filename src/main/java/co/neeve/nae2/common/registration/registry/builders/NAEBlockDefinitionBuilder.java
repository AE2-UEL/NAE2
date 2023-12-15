package co.neeve.nae2.common.registration.registry.builders;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IPreInitComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockDefinition;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.IFeature;
import co.neeve.nae2.common.integration.jei.NAEJEIPlugin;
import co.neeve.nae2.common.registration.definitions.CreativeTab;
import co.neeve.nae2.common.registration.registry.Registry;
import co.neeve.nae2.common.registration.registry.rendering.NAEBlockRendering;
import co.neeve.nae2.common.registration.registry.rendering.NAEItemRendering;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class NAEBlockDefinitionBuilder implements INAEBlockBuilder {
	private final Registry registry;

	private final String registryName;

	private final Supplier<? extends Block> blockSupplier;

	private final List<BiFunction<Block, Item, IBootstrapComponent>> bootstrapComponents = new ArrayList<>();
	private final CreativeTabs creativeTab = CreativeTab.instance;
	@Nullable
	private IFeature[] features = null;
	private TileEntityDefinition tileEntityDefinition;

	private boolean disableItem = false;

	private Function<Block, ItemBlock> itemFactory;

	@SideOnly(Side.CLIENT)
	private NAEBlockRendering blockRendering;

	@SideOnly(Side.CLIENT)
	private NAEItemRendering itemRendering;

	@SideOnly(Side.CLIENT)
	private boolean jeiDescription;

	public NAEBlockDefinitionBuilder(Registry registry, String id, Supplier<? extends Block> blockSupplier) {
		this.registry = registry;
		this.registryName = id;
		this.blockSupplier = blockSupplier;

		if (Platform.isClient()) {
			this.blockRendering = new NAEBlockRendering();
			this.itemRendering = new NAEItemRendering();
		}
	}

	@Override
	public NAEBlockDefinitionBuilder bootstrap(BiFunction<Block, Item, IBootstrapComponent> callback) {
		this.bootstrapComponents.add(callback);
		return this;
	}

	@Override
	public INAEBlockBuilder features(IFeature... features) {
		this.features = features;
		return this;
	}

	@Override
	public NAEBlockDefinitionBuilder rendering(BlockRenderingCustomizer callback) {
		if (Platform.isClient()) {
			this.customizeForClient(callback);
		}

		return this;
	}

	@Override
	public INAEBlockBuilder tileEntity(TileEntityDefinition tileEntityDefinition) {
		this.tileEntityDefinition = tileEntityDefinition;
		return this;
	}

	@Override
	public INAEBlockBuilder useCustomItemModel() {
		this.rendering(new BlockRenderingCustomizer() {
			@Override
			@SideOnly(Side.CLIENT)
			public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
				var model = new ModelResourceLocation(new ResourceLocation(Tags.MODID,
					NAEBlockDefinitionBuilder.this.registryName), "inventory");
				itemRendering.model(model).variants(model);
			}
		});

		return this;
	}

	@Override
	public INAEBlockBuilder item(Function<Block, ItemBlock> factory) {
		this.itemFactory = factory;
		return this;
	}

	@Override
	public INAEBlockBuilder disableItem() {
		this.disableItem = true;
		return this;
	}

	@SideOnly(Side.CLIENT)
	private void customizeForClient(BlockRenderingCustomizer callback) {
		callback.customize(this.blockRendering, this.itemRendering);
	}

	@Override
	public INAEBlockBuilder withJEIDescription() {
		if (Platform.isClient()) {
			this.jeiDescription = true;
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IBlockDefinition> T build() {
		if (this.features != null && Arrays.stream(this.features).noneMatch(IFeature::isEnabled)) {
			return (T) new TileDefinition(this.registryName, null, null);
		}

		// Create block and matching item, and set factory name of both
		var block = this.blockSupplier.get();
		block.setRegistryName(Tags.MODID, this.registryName);
		block.setTranslationKey(Tags.MODID + "." + this.registryName);

		var item = this.constructItemFromBlock(block);
		if (item != null) {
			item.setRegistryName(Tags.MODID, this.registryName);
		}

		// Register the item and block with the game
		this.registry.addBootstrapComponent((IBlockRegistrationComponent) (side, registry) -> registry.register(block));
		if (item != null) {
			this.registry.addBootstrapComponent((IItemRegistrationComponent) (side, registry) -> registry.register(item));
		}

		block.setCreativeTab(this.creativeTab);

		// Register all extra handlers
		this.bootstrapComponents.forEach(component -> this.registry.addBootstrapComponent(component.apply(block,
			item)));

		if (this.tileEntityDefinition != null && block instanceof AEBaseTileBlock) {
			((AEBaseTileBlock) block).setTileEntity(this.tileEntityDefinition.getTileEntityClass());
			if (this.tileEntityDefinition.getName() == null) {
				this.tileEntityDefinition.setName(this.registryName);
			}

		}

		if (Platform.isClient()) {
			if (block instanceof AEBaseTileBlock tileBlock) {
				this.blockRendering.apply(this.registry, block, tileBlock.getTileEntityClass());
			} else {
				this.blockRendering.apply(this.registry, block, null);
			}

			if (item != null) {
				this.itemRendering.apply(this.registry, item);
			}
		}

		final T definition;

		if (block instanceof AEBaseTileBlock) {
			this.registry.addBootstrapComponent((IPreInitComponent) side ->
				AEBaseTile.registerTileItem(
					this.tileEntityDefinition == null ? ((AEBaseTileBlock) block).getTileEntityClass() :
						this.tileEntityDefinition.getTileEntityClass(),
					new BlockStackSrc(block, 0, ActivityState.Enabled)));

			if (this.tileEntityDefinition != null) {
				this.registry.tileEntityComponent.addTileEntity(this.tileEntityDefinition);
			}

			definition = (T) new TileDefinition(this.registryName, (AEBaseTileBlock) block, item);
		} else {
			definition = (T) new BlockDefinition(this.registryName, block, item);
		}

		if (Platform.isClient() && Platform.isModLoaded("jei")) {
			if (this.jeiDescription && item != null) {
				NAEJEIPlugin.registerDescription(definition, item.getTranslationKey() + ".desc");
			}
		}

		return definition;
	}

	@Nullable
	private ItemBlock constructItemFromBlock(Block block) {
		if (this.disableItem) {
			return null;
		}

		if (this.itemFactory != null) {
			return this.itemFactory.apply(block);
		} else if (block instanceof AEBaseBlock) {
			return new AEBaseItemBlock(block);
		} else {
			return new ItemBlock(block);
		}
	}
}
