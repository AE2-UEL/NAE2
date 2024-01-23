package co.neeve.nae2.common.helpers.exposer;

import co.neeve.nae2.NAE2;
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

public class ExposerBootstrapper implements ICapabilityProvider {
	private final IExposerHost host;
	private final EnumSet<EnumFacing> targets;
	private final Object2ObjectMap<Capability<?>, IExposerHandler<?>> exposers = new Object2ObjectOpenHashMap<>();

	public ExposerBootstrapper(IExposerHost host, EnumSet<EnumFacing> targets) {
		this.host = host;
		this.targets = targets;
	}

	/**
	 * Returns whether the capability is registered for handling by the Exposer API.
	 *
	 * @param capability Capability to check
	 * @param facing     Facing to check
	 * @return Whether the capability is registered
	 */
	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
		return (facing == null || this.targets.contains(facing))
			&& NAE2.api().exposer().isCapabilityRegistered(capability);
	}

	/**
	 * Returns the exposer handler for the given capability.
	 *
	 * @param capability Capability to get handler for
	 * @param facing     Facing to get handler for
	 * @param <T>        Type of stack handled by the handler
	 * @return Exposer handler for the capability, or null if the capability is not registered
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
		return facing == null || this.targets.contains(facing)
			? (T) this.exposers.computeIfAbsent(capability, (c) -> NAE2.api().exposer().createHandler(this.host, c))
			: null;
	}
}
