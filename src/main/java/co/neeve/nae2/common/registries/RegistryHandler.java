package co.neeve.nae2.common.registries;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.core.Api;
import appeng.core.ApiDefinitions;
import appeng.core.features.AEFeature;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.features.subfeatures.UpgradeFeatures;
import co.neeve.nae2.common.items.cells.vc.VoidCellHandler;
import co.neeve.nae2.common.items.cells.vc.VoidCraftingHandler;
import co.neeve.nae2.common.items.cells.vc.VoidDisassembleHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;

public class RegistryHandler {
	public RegistryHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void postInit() {
		// TODO: move all this.
		if (Features.PATTERN_MULTI_TOOL.isEnabled()) {
			Upgrades.CAPACITY.registerItem(new ItemStack(Items.PATTERN_MULTI_TOOL.getItem()), 3);
		}

		ApiDefinitions definitions = Api.INSTANCE.definitions();
		final IBlocks blocks = definitions.blocks();

		if (Features.UPGRADES.isEnabled()) {
			// Hyper-Acceleration.
			if (UpgradeFeatures.HYPER_ACCELERATION.isEnabled()) {
				var hyper = co.neeve.nae2.common.registries.Upgrades.HYPER_ACCELERATION;
				hyper.registerItem(blocks.iOPort(), 3);
			}
		}

		// Void Cells.
		if (Features.VOID_CELLS.isEnabled()) {
			AEApi.instance().registries().cell().addCellHandler(new VoidCellHandler());
		}
	}

	@SubscribeEvent
	public void registerItems(final RegistryEvent.Register<Item> event) {
		for (Items itemDef : Items.values()) {
			if (!itemDef.isEnabled()) continue;

			event.getRegistry().register(itemDef.getItem());
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				ModelLoader.setCustomModelResourceLocation(itemDef.getItem(), 0,
					new ModelResourceLocation(Objects.requireNonNull(itemDef.getItem().getRegistryName()), "inventory"
					));
			}
		}

		for (InternalItems itemDef : InternalItems.values()) {
			if (!itemDef.isEnabled()) continue;

			event.getRegistry().register(itemDef.getItem());
		}

		for (Blocks blockDef : Blocks.values()) {
			if (!blockDef.isEnabled()) continue;

			event.getRegistry().register(blockDef.getItemBlock());
		}
	}

	@SubscribeEvent
	public void remap(final RegistryEvent.MissingMappings<Item> event) {
		event.getMappings().forEach(x -> {
			if (x.key.getNamespace().equals(Tags.MODID) && x.key.getPath().equals("pattern_multiplier")) {
				x.remap(Items.PATTERN_MULTI_TOOL.getItem());
			}
		});
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		if (Features.VOID_CELLS.isEnabled() && AEFeature.ENABLE_DISASSEMBLY_CRAFTING.isEnabled()) {
			IForgeRegistry<IRecipe> registry = event.getRegistry();

			registry.register(new VoidDisassembleHandler().setRegistryName("disassemble"));

			AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent(stack -> {
				registry.register(new VoidCraftingHandler(Materials.CELL_VOID_PART.getStack(), stack,
					Items.FLUID_STORAGE_CELL_VOID.getStack()).setRegistryName("fluid_storage_cell_void"));

				registry.register(new VoidCraftingHandler(stack, Materials.CELL_VOID_PART.getStack(),
					Items.STORAGE_CELL_VOID.getStack()).setRegistryName("storage_cell_void"));
			});
		}
	}

	@SubscribeEvent
	public void registerBlocks(final RegistryEvent.Register<Block> event) {
		for (Blocks blockDef : Blocks.values()) {
			if (!blockDef.isEnabled()) return;

			event.getRegistry().register(blockDef.getBlock());
		}
	}


	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(final ModelRegistryEvent event) {
		for (Blocks blockDef : Blocks.values()) {
			if (!blockDef.isEnabled()) return;
			
			ModelLoader.setCustomModelResourceLocation(blockDef.getItemBlock(), 0,
				new ModelResourceLocation(blockDef.getResourceLocation(), "inventory"));
		}
	}
}
