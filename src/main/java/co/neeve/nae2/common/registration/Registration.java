package co.neeve.nae2.common.registration;

import appeng.bootstrap.IModelRegistry;
import appeng.bootstrap.components.*;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import co.neeve.nae2.common.recipes.handlers.DisassembleRecipe;
import co.neeve.nae2.common.registration.definitions.*;
import co.neeve.nae2.common.registration.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Registration {
	private final Blocks blocks;
	private final Registry registry;
	private final Items items;
	private final Materials materials;
	private final Parts parts;
	private final Upgrades upgrades;

	public Registration() {
		MinecraftForge.EVENT_BUS.register(this);

		this.registry = new Registry();
		this.materials = new Materials(this.registry);
		this.items = new Items(this.registry);
		this.parts = new Parts(this.registry);
		this.upgrades = new Upgrades(this.registry);
		this.blocks = new Blocks(this.registry);
	}

	public void preInit(FMLPreInitializationEvent event) {
		this.registry.getBootstrapComponents(IPreInitComponent.class)
			.forEachRemaining(b -> b.preInitialize(event.getSide()));
	}

	public void init(FMLInitializationEvent event) {
		this.registry.getBootstrapComponents(IInitComponent.class)
			.forEachRemaining(b -> b.initialize(event.getSide()));
	}

	public void postInit(FMLPostInitializationEvent event) {
		this.registry.getBootstrapComponents(IPostInitComponent.class)
			.forEachRemaining(b -> b.postInitialize(event.getSide()));
	}

	@SubscribeEvent
	public void registerItems(final RegistryEvent.Register<Item> event) {
		final var registry = event.getRegistry();
		final var side = FMLCommonHandler.instance().getEffectiveSide();
		this.registry.getBootstrapComponents(IItemRegistrationComponent.class)
			.forEachRemaining(b -> b.itemRegistration(side, registry));
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		final var registry = event.getRegistry();
		final var side = FMLCommonHandler.instance().getEffectiveSide();

		if (AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_DISASSEMBLY_CRAFTING)) {
			registry.register(new DisassembleRecipe().setRegistryName("disassemble"));
		}

		this.registry.getBootstrapComponents(IRecipeRegistrationComponent.class)
			.forEachRemaining(b -> b.recipeRegistration(side, registry));
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		final var registry = event.getRegistry();
		final var side = FMLCommonHandler.instance().getEffectiveSide();
		this.registry.getBootstrapComponents(IBlockRegistrationComponent.class)
			.forEachRemaining(b -> b.blockRegistration(side, registry));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(final ModelRegistryEvent event) {
		final var registry = new ModelLoaderWrapper();
		final var side = FMLCommonHandler.instance().getEffectiveSide();
		this.registry.getBootstrapComponents(IModelRegistrationComponent.class)
			.forEachRemaining(b -> b.modelRegistration(side, registry));
	}

	public Blocks blocks() {
		return this.blocks;
	}

	public Items items() {
		return this.items;
	}

	public Materials materials() {
		return this.materials;
	}

	public Parts parts() {
		return this.parts;
	}

	public Upgrades upgrades() {
		return this.upgrades;
	}

	private static class ModelLoaderWrapper implements IModelRegistry {

		@Override
		public void registerItemVariants(Item item, ResourceLocation... names) {
			ModelLoader.registerItemVariants(item, names);
		}

		@Override
		public void setCustomModelResourceLocation(Item item, int metadata, ModelResourceLocation model) {
			ModelLoader.setCustomModelResourceLocation(item, metadata, model);
		}

		@Override
		public void setCustomMeshDefinition(Item item, ItemMeshDefinition meshDefinition) {
			ModelLoader.setCustomMeshDefinition(item, meshDefinition);
		}

		@Override
		public void setCustomStateMapper(Block block, IStateMapper mapper) {
			ModelLoader.setCustomStateMapper(block, mapper);
		}
	}
}
