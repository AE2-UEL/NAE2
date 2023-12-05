package co.neeve.nae2.common.registration.registry.rendering;

import co.neeve.nae2.common.registration.registry.interfaces.IDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public interface IModelProvider extends IDefinition {
	ModelResourceLocation getModel();
}
