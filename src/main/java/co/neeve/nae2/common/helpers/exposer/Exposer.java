package co.neeve.nae2.common.helpers.exposer;

import co.neeve.nae2.common.interfaces.IExposerHandler;
import co.neeve.nae2.common.interfaces.IExposerHost;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class Exposer implements ICapabilityProvider {
	private final EnumSet<EnumFacing> targets;
	private final Object2ObjectMap<Capability<?>, IExposerHandler> exposers;

	public Exposer(IExposerHost host, EnumSet<EnumFacing> targets) {
		this.targets = targets;
		this.exposers = createExposers(host);
	}

	protected static Object2ObjectMap<Capability<?>, IExposerHandler> createExposers(IExposerHost host) {
		var map = new Object2ObjectOpenHashMap<Capability<?>, IExposerHandler>();
		var registered = ExposerHandler.getRegisteredHandlers();
		for (var entry : registered.object2ObjectEntrySet()) {
			map.put(entry.getKey(), ExposerHandler.create(host, entry.getKey()));
		}
		return map;
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
		return this.targets.contains(facing) && this.exposers.containsKey(capability);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
		return this.targets.contains(facing) ? (T) this.exposers.getOrDefault(capability, null) : null;
	}
}
