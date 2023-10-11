package co.neeve.nae2.common;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.core.Api;
import appeng.core.ApiDefinitions;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.items.cells.vc.VoidCellHandler;
import co.neeve.nae2.common.items.cells.vc.VoidCraftingHandler;
import co.neeve.nae2.common.items.cells.vc.VoidDisassembleHandler;
import co.neeve.nae2.common.registries.Items;
import co.neeve.nae2.common.registries.Materials;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;

public class RegistryHandler {
	public RegistryHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void postInit() {
		Upgrades.CAPACITY.registerItem(new ItemStack(Items.PATTERN_MULTI_TOOL.getItem()), 3);

		ApiDefinitions definitions = Api.INSTANCE.definitions();
		final IBlocks blocks = definitions.blocks();

		// Hyper-Acceleration.
		var hyper = co.neeve.nae2.common.registries.Upgrades.HYPER_ACCELERATION;
		hyper.registerItem(blocks.iOPort(), 3);

		// Void Cells.
		AEApi.instance().registries().cell().addCellHandler(new VoidCellHandler());
	}

	@SubscribeEvent
	public void registerItems(final RegistryEvent.Register<Item> event) {
		for (Items itemDef : Items.values()) {
			event.getRegistry().register(itemDef.getItem());
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				ModelLoader.setCustomModelResourceLocation(itemDef.getItem(), 0,
					new ModelResourceLocation(Objects.requireNonNull(itemDef.getItem().getRegistryName()), "inventory"
					));
			}
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
		IForgeRegistry<IRecipe> registry = event.getRegistry();
		registry.register(new VoidDisassembleHandler().setRegistryName("disassemble"));

		AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent(stack -> {
			registry.register(new VoidCraftingHandler(Materials.VOID_CELL_COMPONENT.getStack(), stack,
				Items.FLUID_STORAGE_CELL_VOID.getStack()).setRegistryName("fluid_storage_cell_void"));

			registry.register(new VoidCraftingHandler(stack, Materials.VOID_CELL_COMPONENT.getStack(),
				Items.STORAGE_CELL_VOID.getStack()).setRegistryName("storage_cell_void"));
		});

	}
}
