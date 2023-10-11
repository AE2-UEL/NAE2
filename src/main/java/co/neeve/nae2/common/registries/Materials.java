package co.neeve.nae2.common.registries;

import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.items.NAEMaterial;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum Materials {
	VOID_CELL_COMPONENT("cell_void_part");

	private final String id;
	private final String translationKey;
	private ModelResourceLocation modelResourceLocation = null;

	Materials(String id) {
		this.id = id;
		this.translationKey = Tags.MODID + ".material." + this.id + ".name";
		if (Platform.isClientInstall()) {
			this.modelResourceLocation =
				new ModelResourceLocation(Tags.MODID + ":material_" + this.id, "inventory");
		}
	}

	public static Materials getByID(int id) {
		return values()[id];
	}

	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation() {
		return modelResourceLocation;
	}

	public String getTranslationKey() {
		return this.translationKey;
	}

	public ItemStack getStack() {
		return NAEMaterial.instance.getItemStack(this);
	}
}
