//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package co.neeve.nae2.common.registration.registry.components;

import appeng.bootstrap.components.IPreInitComponent;
import co.neeve.nae2.Tags;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class NAEBuiltInModelComponent implements IPreInitComponent {
	private final Map<String, IModel> builtInModels = new HashMap<>();
	private boolean hasInitialized = false;

	public NAEBuiltInModelComponent() {
	}

	public void addModel(String path, IModel model) {
		Preconditions.checkState(!this.hasInitialized);
		this.builtInModels.put(path, model);
	}

	public void preInitialize(Side side) {
		this.hasInitialized = true;
		var loader = new BuiltInModelLoader(this.builtInModels);
		ModelLoaderRegistry.registerLoader(loader);
	}

	public static class BuiltInModelLoader implements ICustomModelLoader {
		private final Map<String, IModel> builtInModels;

		public BuiltInModelLoader(Map<String, IModel> builtInModels) {
			this.builtInModels = ImmutableMap.copyOf(builtInModels);
		}

		public boolean accepts(ResourceLocation modelLocation) {
			return modelLocation.getNamespace().equals(Tags.MODID) && this.builtInModels.containsKey(
				modelLocation.getPath());
		}

		public @NotNull IModel loadModel(ResourceLocation modelLocation) {
			return this.builtInModels.get(modelLocation.getPath());
		}

		@SuppressWarnings("deprecation")
		public void onResourceManagerReload(@NotNull IResourceManager resourceManager) {

			for (var model : this.builtInModels.values()) {
				if (model instanceof IResourceManagerReloadListener) {
					((IResourceManagerReloadListener) model).onResourceManagerReload(resourceManager);
				}
			}

		}
	}
}
