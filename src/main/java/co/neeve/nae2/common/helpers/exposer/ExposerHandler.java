package co.neeve.nae2.common.helpers.exposer;

import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.MachineSource;
import co.neeve.nae2.common.interfaces.IExposerHandler;
import co.neeve.nae2.common.interfaces.IExposerHost;
import org.jetbrains.annotations.Nullable;

public abstract class ExposerHandler<T extends IAEStack<T>> implements IExposerHandler<T>,
	IMEMonitorHandlerReceiver<T> {
	protected final IExposerHost host;
	protected final IActionSource mySrc;
	protected final AENetworkProxy proxy;
	protected IMEMonitor<T> monitor;
	protected boolean isRecursionLocked = false;

	public ExposerHandler(IExposerHost host) {
		this.host = host;
		this.mySrc = new MachineSource(this.host);
		this.proxy = this.host.getProxy();
	}

	protected abstract void onMonitorChange(IMEMonitor<T> oldValue, IMEMonitor<T> newValue);

	protected abstract IStorageChannel<T> getStorageChannel();

	/**
	 * Queries the network for the monitor.
	 *
	 * @return Monitor, or null if the monitor could not be queried
	 */
	@Nullable
	protected IMEMonitor<T> queryMonitor() {
		if (this.proxy.isActive()) {
			try {
				return this.host.getProxy().getStorage().getInventory(this.getStorageChannel());
			} catch (GridAccessException ignored) {}
		}

		return null;
	}

	/**
	 * Returns the monitor storage list.
	 *
	 * @return Storage, or null if the storage could not be queried
	 */
	@Nullable
	protected IItemList<T> getStorageList() {
		if (this.monitor != null) {
			return this.monitor.getStorageList();
		}

		return null;
	}

	/**
	 * Updates the monitor.
	 */
	protected void updateMonitor() {
		var pendingMonitor = this.queryMonitor();
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

	/**
	 * React to network changes.
	 */
	@Override
	public abstract void postChange(IBaseMonitor<T> iBaseMonitor, Iterable<T> iterable,
	                                IActionSource iActionSource);

	/**
	 * React to network changes.
	 */
	@Override
	public abstract void onListUpdate();

	/**
	 * Returns whether the handler is valid for the given grid.
	 */
	@Override
	public final boolean isValid(Object effectiveGrid) {
		try {
			return this.host.getProxy().getGrid() == effectiveGrid;
		} catch (GridAccessException var3) {
			return false;
		}
	}

	/**
	 * Returns the current monitor.
	 *
	 * @return Monitor
	 */
	public IMEMonitor<T> getMonitor() {
		return this.monitor;
	}

	/**
	 * Returns the action source.
	 *
	 * @return Action source
	 */
	public IActionSource getActionSource() {
		return this.mySrc;
	}

	/**
	 * Returns the proxy. In most cases, this is the cached host proxy.
	 *
	 * @return Network proxy
	 */
	public AENetworkProxy getProxy() {
		return this.proxy;
	}

	protected boolean lockRecursion() {
		if (this.isRecursionLocked) {
			return true;
		} else {
			this.isRecursionLocked = true;
			return false;
		}
	}

	protected void unlockRecursion() {
		this.isRecursionLocked = false;
	}
}
