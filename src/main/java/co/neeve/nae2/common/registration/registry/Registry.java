package co.neeve.nae2.common.registration.registry;

import appeng.api.definitions.IItemDefinition;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.IBootstrapComponent;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.ItemStackSrc;
import appeng.util.Platform;
import co.neeve.nae2.common.registration.registry.builders.INAEBlockBuilder;
import co.neeve.nae2.common.registration.registry.builders.INAEItemBuilder;
import co.neeve.nae2.common.registration.registry.builders.NAEBlockDefinitionBuilder;
import co.neeve.nae2.common.registration.registry.builders.NAEItemDefinitionBuilder;
import co.neeve.nae2.common.registration.registry.components.NAEBuiltInModelComponent;
import co.neeve.nae2.common.registration.registry.components.NAEModelOverrideComponent;
import co.neeve.nae2.common.registration.registry.components.NAETileEntityComponent;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Registry {

	public final NAETileEntityComponent tileEntityComponent;
	private final Map<Class<? extends IBootstrapComponent>, List<IBootstrapComponent>> bootstrapComponents;
	@SideOnly(Side.CLIENT)
	private NAEModelOverrideComponent modelOverrideComponent;
	@SideOnly(Side.CLIENT)
	private NAEBuiltInModelComponent builtInModelComponent;

	public Registry() {
		this.bootstrapComponents = new HashMap<>();

		this.tileEntityComponent = new NAETileEntityComponent();
		this.addBootstrapComponent(this.tileEntityComponent);

		if (Platform.isClient()) {
			this.modelOverrideComponent = new NAEModelOverrideComponent();
			this.addBootstrapComponent(this.modelOverrideComponent);

			this.builtInModelComponent = new NAEBuiltInModelComponent();
			this.addBootstrapComponent(this.builtInModelComponent);
		}
	}

	public INAEBlockBuilder block(String id, Supplier<Block> block) {
		return new NAEBlockDefinitionBuilder(this, id, block);
	}

	public INAEItemBuilder item(String id, Supplier<Item> item) {
		return new NAEItemDefinitionBuilder(this, id, item);
	}

	public AEColoredItemDefinition colored(IItemDefinition target, int offset) {
		var definition = new ColoredItemDefinition();

		target.maybeItem().ifPresent(targetItem ->
		{
			for (final var color : AEColor.VALID_COLORS) {
				final var state = ActivityState.from(target.isEnabled());

				definition.add(color, new ItemStackSrc(targetItem, offset + color.ordinal(), state));
			}
		});

		return definition;
	}

	public void addBootstrapComponent(IBootstrapComponent component) {
		Arrays.stream(component.getClass().getInterfaces())
			.filter(IBootstrapComponent.class::isAssignableFrom)
			.forEach(i -> this.addBootstrapComponent((Class<? extends IBootstrapComponent>) i, component));
	}

	private <T extends IBootstrapComponent> void addBootstrapComponent(Class<? extends IBootstrapComponent> eventType,
	                                                                   T component) {
		this.bootstrapComponents.computeIfAbsent(eventType, c -> new ArrayList<>()).add(component);
	}

	@SideOnly(Side.CLIENT)
	public void addBuiltInModel(String path, IModel model) {
		this.builtInModelComponent.addModel(path, model);
	}

	@SideOnly(Side.CLIENT)
	public void addModelOverride(String resourcePath,
	                             BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer) {
		this.modelOverrideComponent.addOverride(resourcePath, customizer);
	}

	public <T extends IBootstrapComponent> Iterator<T> getBootstrapComponents(Class<T> eventType) {
		return (Iterator<T>) this.bootstrapComponents.getOrDefault(eventType, Collections.emptyList()).iterator();
	}
}