package co.neeve.nae2.common.registration.registry.rendering;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import co.neeve.nae2.common.registration.registry.interfaces.DamagedDefinitions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;
import java.util.stream.Collectors;

public class DamagedItemRendering<T extends DamagedDefinitions<?, ?>> extends ItemRenderingCustomizer {
	private final T registry;

	public DamagedItemRendering(T registry) {
		this.registry = registry;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void customize(IItemRendering rendering) {
		rendering.variants(this.registry.getEntries().stream()
			.map(IModelProvider::getModel)
			.collect(Collectors.toList()));

		rendering.meshDefinition(is -> Objects.requireNonNull(this.registry.getType(is)).getModel());
	}
}
