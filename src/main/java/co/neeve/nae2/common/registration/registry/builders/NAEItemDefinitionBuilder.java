package co.neeve.nae2.common.registration.registry.builders;

import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.core.features.ItemDefinition;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.IFeature;
import co.neeve.nae2.common.integration.jei.NAEJEIPlugin;
import co.neeve.nae2.common.registration.definitions.CreativeTab;
import co.neeve.nae2.common.registration.registry.Registry;
import co.neeve.nae2.common.registration.registry.rendering.NAEItemRendering;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NAEItemDefinitionBuilder implements INAEItemBuilder {

	private final Registry registry;

	private final String registryName;

	private final Supplier<Item> itemSupplier;
	private final List<Function<Item, IBootstrapComponent>> boostrapComponents = new ArrayList<>();
	@Nullable
	private IFeature[] features = null;
	private Supplier<IBehaviorDispenseItem> dispenserBehaviorSupplier;

	@SideOnly(Side.CLIENT)
	private NAEItemRendering itemRendering;

	private CreativeTabs creativeTab = CreativeTab.instance;

	private boolean hidden;

	public NAEItemDefinitionBuilder(Registry registry, String registryName, Supplier<Item> itemSupplier) {
		this.registry = registry;
		this.registryName = registryName;
		this.itemSupplier = itemSupplier;
		if (Platform.isClient()) {
			this.itemRendering = new NAEItemRendering();
		}
	}

	@Override
	public INAEItemBuilder bootstrap(Function<Item, IBootstrapComponent> component) {
		this.boostrapComponents.add(component);
		return this;
	}

	public INAEItemBuilder features(IFeature... features) {
		this.features = features;
		return this;
	}

	@Override
	public INAEItemBuilder creativeTab(CreativeTabs tab) {
		this.creativeTab = tab;
		return this;
	}

	@Override
	public INAEItemBuilder rendering(ItemRenderingCustomizer callback) {
		if (Platform.isClient()) {
			this.customizeForClient(callback);
		}

		return this;
	}

	@Override
	public INAEItemBuilder dispenserBehavior(Supplier<IBehaviorDispenseItem> behavior) {
		this.dispenserBehaviorSupplier = behavior;
		return this;
	}

	@SideOnly(Side.CLIENT)
	private void customizeForClient(ItemRenderingCustomizer callback) {
		callback.customize(this.itemRendering);
	}

	@Override
	public INAEItemBuilder hide() {
		this.hidden = true;
		return this;
	}

	@Override
	public ItemDefinition build() {
		if (this.features != null && Arrays.stream(this.features).noneMatch(IFeature::isEnabled)) {
			return new ItemDefinition(this.registryName, null);
		}

		var item = this.itemSupplier.get();
		item.setRegistryName(Tags.MODID, this.registryName);

		var definition = new ItemDefinition(this.registryName, item);

		item.setTranslationKey(Tags.MODID + "." + this.registryName);
		if (!this.hidden) {
			item.setCreativeTab(this.creativeTab);
		}

		// Register all extra handlers
		this.boostrapComponents.forEach(component -> this.registry.addBootstrapComponent(component.apply(item)));

		// Register custom dispenser behavior if requested
		if (this.dispenserBehaviorSupplier != null) {
			this.registry.addBootstrapComponent((IPostInitComponent) side ->
			{
				var behavior = this.dispenserBehaviorSupplier.get();
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, behavior);
			});
		}

		this.registry.addBootstrapComponent((IItemRegistrationComponent) (side, reg) -> reg.register(item));

		if (Platform.isClient()) {
			if (this.hidden && Platform.isModLoaded("jei")) {
				NAEJEIPlugin.registerBlacklist(definition);
			}

			this.itemRendering.apply(this.registry, item);
		}

		return definition;
	}
}