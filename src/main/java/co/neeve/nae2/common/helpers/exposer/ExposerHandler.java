package co.neeve.nae2.common.helpers.exposer;

import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import co.neeve.nae2.common.interfaces.IExposerHandler;
import co.neeve.nae2.common.interfaces.IExposerHost;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExposerHandler<T extends IAEStack<T>> implements IExposerHandler, IMEMonitorHandlerReceiver<T> {
	private static final Object2ObjectMap<Capability<?>, Class<ExposerHandler<?>>> REGISTERED_HANDLERS =
		new Object2ObjectOpenHashMap<>();
	protected final IExposerHost host;
	protected final IActionSource mySrc;
	protected IMEMonitor<T> monitor;

	public ExposerHandler(IExposerHost host) {
		this.host = host;
		this.mySrc = new MachineSource(this.host);
	}

	@SuppressWarnings("unchecked")
	public static <T extends IAEStack<T>> ExposerHandler<T> create(IExposerHost host, Capability<?> capability) {
		var cap = REGISTERED_HANDLERS.get(capability);
		if (cap == null) {
			throw new RuntimeException("Couldn't found handler for capability " + capability.toString());
		}

		try {
			return (ExposerHandler<T>) cap.getConstructor(IExposerHost.class).newInstance(host);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T, U extends T> void registerHandler(Capability<T> capability, Class<U> exposerClass) {
		var cast = (Class<ExposerHandler<?>>) (Object) exposerClass;
		REGISTERED_HANDLERS.put(capability, cast);
	}

	public static Object2ObjectMap<Capability<?>, Class<ExposerHandler<?>>> getRegisteredHandlers() {
		return REGISTERED_HANDLERS;
	}

	protected abstract void onMonitorChange(IMEMonitor<T> oldValue, IMEMonitor<T> newValue);

	protected abstract IStorageChannel<T> getStorageChannel();

	@Nullable
	protected IMEMonitor<T> getMonitor() {
		try {
			return this.host.getProxy().getStorage().getInventory(this.getStorageChannel());
		} catch (GridAccessException ignored) {}

		return null;
	}

	@Nullable
	protected IItemList<T> getStorage() {
		try {
			return this.host.getProxy().getStorage().getInventory(this.getStorageChannel()).getStorageList();
		} catch (GridAccessException ignored) {}

		return null;
	}

	protected void updateMonitor() {
		var pendingMonitor = this.getMonitor();
		if (this.monitor != pendingMonitor) {
			var oldMonitor = this.monitor;
			if (oldMonitor != null) {
				oldMonitor.removeListener(this);
			}

			this.monitor = pendingMonitor;
			if (pendingMonitor != null) {
				try {
					this.monitor.addListener(this, this.host.getProxy().getGrid());
				} catch (GridAccessException ignored) {
					this.monitor = null;
				}
			}

			this.onMonitorChange(oldMonitor, this.monitor);
		}
	}

	@Override
	public abstract void postChange(IBaseMonitor<T> iBaseMonitor, Iterable<T> iterable,
	                                IActionSource iActionSource);

	@Override
	public abstract void onListUpdate();


	@Override
	public final boolean isValid(Object effectiveGrid) {
		try {
			return this.host.getProxy().getGrid() == effectiveGrid;
		} catch (GridAccessException var3) {
			return false;
		}
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
		return false;
	}
}
