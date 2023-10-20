package co.neeve.nae2.common.registries;

import appeng.api.definitions.IItemDefinition;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.subfeatures.UpgradeFeatures;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum Upgrades {
	HYPER_ACCELERATION("hyper_acceleration", UpgradeFeatures.HYPER_ACCELERATION);

	private static Upgrades[] cachedValues;
	private final String translationKey;
	private final Map<ItemStack, Integer> supportedMax = new HashMap<>();
	private UpgradeFeatures feature;
	private ModelResourceLocation modelResourceLocation = null;

	Upgrades(String id) {
		this.translationKey = Tags.MODID + ".upgrade." + id + ".name";
		if (Platform.isClientInstall()) {
			this.modelResourceLocation = new ModelResourceLocation(Tags.MODID + ":upgrade/" + id, "inventory");
		}
	}

	Upgrades(String id, UpgradeFeatures upgradeFeature) {
		this(id);
		this.feature = upgradeFeature;
	}

	@Nullable
	public static Upgrades getByID(int id) {
		if (cachedValues == null) cachedValues = values();

		if (id < 0 || id >= cachedValues.length) return null;
		return cachedValues[id];
	}

	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation() {
		return modelResourceLocation;
	}

	public String getTranslationKey() {
		return this.translationKey;
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

	public boolean isEnabled() {
		return this.feature == null || this.feature.isEnabled();
	}
}
