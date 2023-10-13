package co.neeve.nae2.common.registries;

import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.items.NAEMaterial;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public enum Materials {
	CELL_VOID_PART("cell_void_part", Features.VOID_CELLS);

	private final String translationKey;
	private Features feature;
	private ModelResourceLocation modelResourceLocation = null;

	Materials(String id) {
		this.translationKey = Tags.MODID + ".material." + id + ".name";
		if (Platform.isClientInstall()) {
			this.modelResourceLocation =
				new ModelResourceLocation(Tags.MODID + ":material_" + id, "inventory");
		}
	}

	Materials(String id, Features feature) {
		this(id);

		this.feature = feature;
	}

	public static Materials getByID(int id) {
		return values()[id];
	}

	@Nullable
	public static Materials getByName(String name) {
		name = name.toUpperCase();

		try {
			return Materials.valueOf(name);
		} catch (IllegalArgumentException err) {
			return null;
		}
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

	public boolean isEnabled() {
		return this.feature == null || this.feature.isEnabled();
	}
}
