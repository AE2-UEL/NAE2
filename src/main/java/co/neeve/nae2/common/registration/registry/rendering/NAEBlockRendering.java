package co.neeve.nae2.common.registration.registry.rendering;

import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.components.BlockColorComponent;
import appeng.bootstrap.components.StateMapperComponent;
import appeng.bootstrap.components.TesrComponent;
import appeng.client.render.model.AutoRotatingModel;
import co.neeve.nae2.common.registration.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class NAEBlockRendering implements IBlockRendering, ISelectiveResourceReloadListener {

	@SideOnly(Side.CLIENT)
	private final Map<String, IModel> builtInModels = new HashMap<>();
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	private final List<IResourceManagerReloadListener> reloads = new ArrayList<>();
	@SideOnly(Side.CLIENT)
	private BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> modelCustomizer;
	@SideOnly(Side.CLIENT)
	private IBlockColor blockColor;
	@SideOnly(Side.CLIENT)
	private TileEntitySpecialRenderer<?> tesr;
	@SideOnly(Side.CLIENT)
	private IStateMapper stateMapper;

	public NAEBlockRendering() {
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockRendering modelCustomizer(BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer) {
		this.modelCustomizer = customizer;
		return this;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IBlockRendering blockColor(IBlockColor blockColor) {
		this.blockColor = blockColor;
		return this;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IBlockRendering tesr(TileEntitySpecialRenderer<?> tesr) {
		this.tesr = tesr;
		return this;
	}

	@Override
	public IBlockRendering builtInModel(String name, IModel model) {
		this.builtInModels.put(name, model);
		return this;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IBlockRendering stateMapper(IStateMapper mapper) {
		this.stateMapper = mapper;
		return this;
	}

	public void apply(Registry registry, Block block, Class<?> tileEntityClass) {
		if (this.tesr != null) {
			if (tileEntityClass == null) {
				throw new IllegalStateException("Tried to register a TESR for " + block + " even though no tile " +
					"entity" +
					" has been specified.");
			}
			//noinspection rawtypes
			registry.addBootstrapComponent(new TesrComponent(tileEntityClass, this.tesr));
		}

		if (this.modelCustomizer != null) {
			registry.addModelOverride(Objects.requireNonNull(block.getRegistryName()).getPath(), this.modelCustomizer);
		} else if (block instanceof AEBaseTileBlock) {
			// This is a default rotating model if the base-block uses an AE tile entity which exposes UP/FRONT as
			// extended props
			registry.addModelOverride(Objects.requireNonNull(block.getRegistryName()).getPath(), (l, m) -> {
				var model = new AutoRotatingModel(m);
				NAEBlockRendering.this.reloads.add(model);
				return model;
			});
		}

		// TODO : 1.12
		this.builtInModels.forEach(registry::addBuiltInModel);

		if (this.blockColor != null) {
			registry.addBootstrapComponent(new BlockColorComponent(block, this.blockColor));
		}

		if (this.stateMapper != null) {
			registry.addBootstrapComponent(new StateMapperComponent(block, this.stateMapper));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onResourceManagerReload(@NotNull IResourceManager resourceManager,
	                                    Predicate<IResourceType> resourcePredicate) {
		if (resourcePredicate.test(VanillaResourceType.MODELS)) {
			this.reloads.forEach(listener -> listener.onResourceManagerReload(resourceManager));
		}
	}

}