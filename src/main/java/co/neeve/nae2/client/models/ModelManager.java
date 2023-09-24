package co.neeve.nae2.client.models;

import appeng.api.AEApi;
import appeng.api.parts.IPartModel;
import appeng.api.parts.IPartModels;
import co.neeve.nae2.common.interfaces.IPartModelProvider;
import co.neeve.nae2.common.registries.Items;
import co.neeve.nae2.common.registries.Parts;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ModelManager {
	@SubscribeEvent
	public void modelRegistryEvent(ModelRegistryEvent event) {
		IPartModels partModels = AEApi.instance().registries().partModels();

		for (var part : Parts.values()) {
			ModelLoader.setCustomModelResourceLocation(Items.BASE_PART.getItem(), part.ordinal(),
				part.getModelResourceLocation());

			if (IPartModelProvider.class.isAssignableFrom(part.getClazz())) {
				var iPartModelProviderClass = part.getClazz().asSubclass(IPartModelProvider.class);
				List<IPartModel> models;
				try {
					//noinspection unchecked XD
					models = (List<IPartModel>) iPartModelProviderClass.getMethod("getModels").invoke(null);
				} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}

				if (models != null) {
					for (var model : models) {
						partModels.registerModels(model.getModels());
					}
				}
			}
		}
	}
}
