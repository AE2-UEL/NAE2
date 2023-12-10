package co.neeve.nae2.common.registration.definitions;

import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItemDefinition;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.core.Api;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.IStackSrc;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.IFeature;
import co.neeve.nae2.common.features.subfeatures.UpgradeFeatures;
import co.neeve.nae2.common.items.NAEBaseItemUpgrade;
import co.neeve.nae2.common.registration.registry.Registry;
import co.neeve.nae2.common.registration.registry.interfaces.DamagedDefinitions;
import co.neeve.nae2.common.registration.registry.rendering.DamagedItemRendering;
import co.neeve.nae2.common.registration.registry.rendering.IModelProvider;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Upgrades implements DamagedDefinitions<DamagedItemDefinition, Upgrades.UpgradeType> {
	private final Object2ObjectOpenHashMap<String, DamagedItemDefinition> byId =
		new Object2ObjectOpenHashMap<>();
	private final IItemDefinition hyperAcceleration;
	private final NAEBaseItemUpgrade upgrade;

	public Upgrades(Registry registry) {
		this.upgrade = new NAEBaseItemUpgrade();
		registry.item("upgrade", () -> this.upgrade)
			.rendering(new DamagedItemRendering<>(this))
			.build();

		this.hyperAcceleration = this.createUpgrade(this.upgrade, UpgradeType.HYPER_ACCELERATION);
		if (this.hyperAcceleration.isEnabled()) {
			registry.addBootstrapComponent((IPostInitComponent) r -> {
				var definitions = Api.INSTANCE.definitions();
				final IBlocks blocks = definitions.blocks();

				UpgradeType.HYPER_ACCELERATION.registerItem(blocks.iOPort(), 3);
			});
		}
	}

	@NotNull
	private DamagedItemDefinition createUpgrade(NAEBaseItemUpgrade material, UpgradeType upgradeType) {
		var def = new DamagedItemDefinition(upgradeType.getId(),
			material.createUpgrade(upgradeType));

		this.byId.put(upgradeType.getId(), def);
		return def;
	}

	public Optional<UpgradeType> getById(int itemDamage) {
		return Optional.ofNullable(UpgradeType.getCachedValues().getOrDefault(itemDamage, null));
	}

	public Optional<DamagedItemDefinition> getById(String id) {
		return Optional.ofNullable(this.byId.getOrDefault(id, null));
	}

	public IItemDefinition hyperAcceleration() {
		return this.hyperAcceleration;
	}

	@Override
	public Collection<UpgradeType> getEntries() {
		return UpgradeType.getCachedValues().values();
	}

	@Nullable
	@Override
	public UpgradeType getType(ItemStack is) {
		return this.upgrade.getType(is);
	}

	public enum UpgradeType implements IModelProvider {

		HYPER_ACCELERATION("hyper_acceleration", UpgradeFeatures.HYPER_ACCELERATION);

		private static Int2ObjectLinkedOpenHashMap<UpgradeType> cachedValues;
		private final Map<ItemStack, Integer> supportedMax = new HashMap<>();
		private final String id;
		private final IFeature features;
		private final String translationKey;
		private final ModelResourceLocation model;
		private final int damageValue = this.ordinal();
		private boolean isRegistered;
		private Item itemInstance;
		private IStackSrc stackSrc;

		UpgradeType(String id, IFeature features) {
			this.id = id;
			this.features = features;
			this.translationKey = "item." + Tags.MODID + ".upgrade." + id;
			this.model = new ModelResourceLocation(new ResourceLocation(Tags.MODID, "upgrade/" + id), "inventory");
		}

		public static Int2ObjectLinkedOpenHashMap<UpgradeType> getCachedValues() {
			if (cachedValues == null) {
				cachedValues = new Int2ObjectLinkedOpenHashMap<>();
				Arrays.stream(values()).forEach((upgradeType -> cachedValues.put(upgradeType.ordinal(),
					upgradeType)));
			}
			return cachedValues;
		}

		public String getId() {
			return this.id;
		}

		public IFeature getFeature() {
			return this.features;
		}

		public String getTranslationKey() {
			return this.translationKey;
		}

		public ItemStack stack(final int size) {
			return new ItemStack(this.getItemInstance(), size, this.getDamageValue());
		}

		public boolean isRegistered() {
			return this.isRegistered;
		}

		public boolean isEnabled() {
			return this.features.isEnabled();
		}

		public void markReady() {
			this.isRegistered = true;
		}

		public int getDamageValue() {
			return this.damageValue;
		}

		public Item getItemInstance() {
			return this.itemInstance;
		}

		public void setItemInstance(final Item itemInstance) {
			this.itemInstance = itemInstance;
		}

		public UpgradeStackSrc getStackSrc() {
			return (UpgradeStackSrc) this.stackSrc;
		}

		public void setStackSrc(final UpgradeStackSrc stackSrc) {
			this.stackSrc = stackSrc;
		}

		public ModelResourceLocation getModel() {
			return this.model;
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

	public static class UpgradeStackSrc implements IStackSrc {
		private final UpgradeType src;
		private final boolean enabled;

		public UpgradeStackSrc(UpgradeType src, boolean enabled) {
			Preconditions.checkNotNull(src);
			this.src = src;
			this.enabled = enabled;
		}

		public ItemStack stack(int stackSize) {
			return this.src.stack(stackSize);
		}

		public Item getItem() {
			return this.src.getItemInstance();
		}

		public int getDamage() {
			return this.src.getDamageValue();
		}

		public boolean isEnabled() {
			return this.enabled;
		}
	}

}
