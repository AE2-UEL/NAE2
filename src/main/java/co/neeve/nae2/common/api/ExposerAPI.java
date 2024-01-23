package co.neeve.nae2.common.api;

import appeng.api.storage.data.IAEStack;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.interfaces.IExposerHandler;
import co.neeve.nae2.common.interfaces.IExposerHost;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * API for the Exposer block/part.
 *
 * @author NotMyWing
 */
public class ExposerAPI {
	@SuppressWarnings("rawtypes")
	private final DataHolder dataHolder = new DataHolder();

	@SideOnly(Side.CLIENT)
	public void addTooltipInformation(ItemStack stack, World world, List<String> lines,
	                                  ITooltipFlag advancedTooltips) {
		lines.add(I18n.format("nae2.exposer.tooltip"));

		var registered = this.getRegisteredHandlers();
		if (registered.isEmpty()) {
			lines.add("");
			lines.add(I18n.format("nae2.exposer.noneregistered"));
		} else {
			lines.add("");
			lines.add(I18n.format("nae2.exposer.registered"));

			for (var handler : registered.object2ObjectEntrySet()) {
				var name = handler.getKey().getName();

				// If name is a class path, strip everything but the name.
				if (name.contains(".")) {
					name = name.substring(name.lastIndexOf('.') + 1);
				}

				lines.add(" - "
					+ "§6" + name + "§r"
					+ " (" + handler.getValue().getMod().getAnnotation(Mod.class).name() + ")");
			}
		}
	}

	/**
	 * Creates a handler for the given host and capability.
	 *
	 * @param host       Host to create the handler for
	 * @param capability Capability to create the handler for
	 * @param <T>        Type of stack handled by the handler
	 * @return Handler for the given host and capability
	 */
	@SuppressWarnings("unchecked")
	public <T extends IAEStack<T>> IExposerHandler<T> createHandler(IExposerHost host, Capability<?> capability) {
		var info = this.getHandlerInfo(capability);
		if (info == null) {
			return null;
		}

		try {
			var result = info.getHandlerClass().getConstructor(IExposerHost.class).newInstance(host);
			NAE2.logger().debug("Created exposer handler for capability {} at {}: {}",
				capability,
				host.getProxy().getLocation(),
				result);

			return (IExposerHandler<T>) result;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
		         NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers a handler for the given capability.
	 *
	 * @param mod          Mod class for the handler
	 * @param capability   Capability to register the handler for
	 * @param exposerClass Handler class
	 * @param <T>          Type of stack handled by the handler
	 */
	@SuppressWarnings("unchecked")
	public <T extends IAEStack<T>> void registerHandler(Class<?> mod, Capability<?> capability,
	                                                    Class<? extends IExposerHandler<T>> exposerClass) {
		var handlerInfo = new HandlerInfo<>(exposerClass, mod);
		this.dataHolder.registeredHandlers.put(capability, handlerInfo);
	}

	/**
	 * Returns an immutable map of registered handlers.
	 *
	 * @return Map of registered handlers
	 */
	@SuppressWarnings("unchecked")
	public <T extends IAEStack<T>, U extends Capability<?>> Object2ObjectMap<U, HandlerInfo<T>> getRegisteredHandlers() {
		return this.dataHolder.immutableWrapper;
	}

	/**
	 * Returns whether the capability is registered for handling by the Exposer API.
	 *
	 * @param capability Capability to check
	 * @return Whether the capability is registered
	 */
	public boolean isCapabilityRegistered(Capability<?> capability) {
		return this.dataHolder.registeredHandlers.containsKey(capability);
	}

	/**
	 * Returns information about the handler for the given capability.
	 *
	 * @param capability Capability to get handler info for
	 * @return Handler info for the capability, or null if the capability is not registered
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends IAEStack<T>, U extends Capability<?>> HandlerInfo<T> getHandlerInfo(U capability) {
		return (HandlerInfo<T>) this.dataHolder.registeredHandlers.getOrDefault(capability, null);
	}

	public static final class HandlerInfo<T extends IAEStack<T>> {
		private final Class<? extends IExposerHandler<T>> handlerClass;
		private final Class<?> mod;

		private HandlerInfo(@NotNull Class<? extends IExposerHandler<T>> handlerClass, @NotNull Class<?> mod) {
			// Check mod for the Mod annotation and throw if it doesn't exist.
			if (!mod.isAnnotationPresent(Mod.class)) {
				throw new IllegalArgumentException("Mod class must be annotated with @Mod");
			}

			this.handlerClass = handlerClass;
			this.mod = mod;
		}

		/**
		 * Returns the handler class.
		 *
		 * @return Handler class
		 */
		public Class<? extends IExposerHandler<T>> getHandlerClass() {return this.handlerClass;}

		/**
		 * Returns the mod class for the handler.
		 *
		 * @return Mod class for the handler
		 */
		public Class<?> getMod() {return this.mod;}
	}

	/**
	 * Holds the data for the Exposer API.
	 * Necessary to make this its own class because of the generic type.
	 */
	private static class DataHolder<T extends IAEStack<T>, U extends Capability<?>> {
		private final Object2ObjectMap<U, HandlerInfo<T>> registeredHandlers =
			new Object2ObjectOpenHashMap<>();

		private final Object2ObjectMap<U, HandlerInfo<T>> immutableWrapper =
			Object2ObjectMaps.unmodifiable(this.registeredHandlers);
	}
}
