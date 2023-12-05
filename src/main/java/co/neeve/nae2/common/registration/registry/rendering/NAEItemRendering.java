package co.neeve.nae2.common.registration.registry.rendering;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.components.ItemColorComponent;
import appeng.bootstrap.components.ItemMeshDefinitionComponent;
import appeng.bootstrap.components.ItemModelComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import co.neeve.nae2.common.registration.registry.Registry;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NAEItemRendering implements IItemRendering {

	@SideOnly(Side.CLIENT)
	private final Map<Integer, ModelResourceLocation> itemModels = new HashMap<>();
	@SideOnly(Side.CLIENT)
	private final Set<ResourceLocation> variants = new HashSet<>();
	@SideOnly(Side.CLIENT)
	private final Map<String, IModel> builtInModels = new HashMap<>();
	@SideOnly(Side.CLIENT)
	private IItemColor itemColor;
	@SideOnly(Side.CLIENT)
	private ItemMeshDefinition itemMeshDefinition;

	@Override
	@SideOnly(Side.CLIENT)
	public IItemRendering meshDefinition(ItemMeshDefinition meshDefinition) {
		this.itemMeshDefinition = meshDefinition;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemRendering model(int meta, ModelResourceLocation model) {
		this.itemModels.put(meta, model);
		return this;
	}

	@Override
	public IItemRendering variants(Collection<ResourceLocation> resources) {
		this.variants.addAll(resources);
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemRendering color(IItemColor itemColor) {
		this.itemColor = itemColor;
		return this;
	}

	@Override
	public IItemRendering builtInModel(String name, IModel model) {
		this.builtInModels.put(name, model);
		return this;
	}

	public void apply(Registry registry, Item item) {
		if (this.itemMeshDefinition != null) {
			registry.addBootstrapComponent(new ItemMeshDefinitionComponent(item, this.itemMeshDefinition));
		}

		if (!this.itemModels.isEmpty()) {
			registry.addBootstrapComponent(new ItemModelComponent(item, this.itemModels));
		}

		Set<ResourceLocation> resources = new HashSet<>(this.variants);

		// Register a default item model if neither items by meta nor an item mesh definition exist
		if (this.itemMeshDefinition == null && this.itemModels.isEmpty()) {
			ModelResourceLocation model;

			// For block items, the default will try to use the default state of the associated block
			if (item instanceof ItemBlock) {
				var block = ((ItemBlock) item).getBlock();

				// We can only do this once the blocks are actually registered...
				var helper = new StateMapperHelper(item.getRegistryName());
				model = helper.getModelResourceLocation(block.getDefaultState());
			} else {
				model = new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory");
			}
			registry.addBootstrapComponent(new ItemModelComponent(item, ImmutableMap.of(0, model)));
		}

		// TODO : 1.12
		this.builtInModels.forEach(registry::addBuiltInModel);

		if (!resources.isEmpty()) {
			registry.addBootstrapComponent(new ItemVariantsComponent(item, resources));
		} else if (this.itemMeshDefinition != null) {
			// Adding an empty variant list here will prevent Vanilla from trying to load the default item model in
			// this
			// case
			registry.addBootstrapComponent(new ItemVariantsComponent(item, Collections.emptyList()));
		}

		if (this.itemColor != null) {
			registry.addBootstrapComponent(new ItemColorComponent(item, this.itemColor));
		}
	}

	private static class StateMapperHelper extends StateMapperBase {

		private final ResourceLocation registryName;

		public StateMapperHelper(ResourceLocation registryName) {
			this.registryName = registryName;
		}

		@Override
		protected @NotNull ModelResourceLocation getModelResourceLocation(IBlockState state) {
			return new ModelResourceLocation(this.registryName, this.getPropertyString(state.getProperties()));
		}
	}
}