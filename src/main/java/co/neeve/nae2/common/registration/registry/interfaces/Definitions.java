package co.neeve.nae2.common.registration.registry.interfaces;

import appeng.api.definitions.IItemDefinition;

import java.util.Optional;

public interface Definitions<T extends IItemDefinition> {
	Optional<T> getById(String id);
}
