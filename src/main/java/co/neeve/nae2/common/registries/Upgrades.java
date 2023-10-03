package co.neeve.nae2.common.registries;

import appeng.api.definitions.IItemDefinition;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public enum Upgrades {
	HYPER_ACCELERATION("hyper_acceleration");

	private final String id;
	private final String translationKey;
	private final Map<ItemStack, Integer> supportedMax = new HashMap<>();
	private ModelResourceLocation modelResourceLocation = null;

	Upgrades(String id) {
		this.id = id;
		this.translationKey = Tags.MODID + ".upgrade." + this.id + ".name";
		if (Platform.isClientInstall()) {
			this.modelResourceLocation = new ModelResourceLocation(Tags.MODID + ":upgrade/" + this.id, "inventory");
		}
	}

	public static Upgrades getByID(int id) {
		return values()[id];
	}

	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation() {
		return modelResourceLocation;
	}

	public String getTranslationKey() {
		return this.translationKey;
	}

	public String getId() {
		return id;
	}

	public Map<ItemStack, Integer> getSupported() {
		return this.supportedMax;
	}

	public void registerItem(IItemDefinition item, int maxSupported) {
		item.maybeStack(1).ifPresent((is) -> this.registerItem(is, maxSupported));
	}

	public void registerItem(ItemStack stack, int maxSupported) {
		if (stack != null) {
			this.supportedMax.put(stack, maxSupported);
		}

	}
}
