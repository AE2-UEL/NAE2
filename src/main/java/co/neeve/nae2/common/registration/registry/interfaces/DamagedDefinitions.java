package co.neeve.nae2.common.registration.registry.interfaces;

import appeng.api.definitions.IItemDefinition;
import co.neeve.nae2.common.registration.registry.rendering.IModelProvider;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;

public interface DamagedDefinitions<T extends IItemDefinition, U extends IModelProvider> extends Definitions<T> {
	Collection<U> getEntries();

	@Nullable
	U getType(ItemStack is);
}
