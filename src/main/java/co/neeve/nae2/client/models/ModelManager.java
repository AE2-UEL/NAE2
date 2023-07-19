package co.neeve.nae2.client.models;

import co.neeve.nae2.common.registries.Items;
import co.neeve.nae2.common.registries.Parts;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelManager {
	public static void register() {
		for (var part : Parts.values()) {
			ModelLoader.setCustomModelResourceLocation(Items.BASE_PART.getItem(), part.ordinal(),
				part.getModelResourceLocation());
		}
	}
}
