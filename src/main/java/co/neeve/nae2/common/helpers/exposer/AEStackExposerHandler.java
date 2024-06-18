package co.neeve.nae2.common.helpers.exposer;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import co.neeve.nae2.common.helpers.ObjectIndexableLinkedOpenHashSet;
import co.neeve.nae2.common.interfaces.IExposerHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Boilerplate class to expose stacks in a network.
 *
 * @param <T> Stack type
 */
public abstract class AEStackExposerHandler<T extends IAEStack<T>> extends ExposerHandler<T> {
	protected final @NotNull ObjectIndexableLinkedOpenHashSet<T> cache = new ObjectIndexableLinkedOpenHashSet<>();

	public AEStackExposerHandler(IExposerHost host) {
		super(host);
	}

	/**
	 * Refreshes the cache of stacks in the network.
	 */
	protected void refreshCache() {
		this.cache.clear();

		var storage = this.getStorageList();
		if (storage != null) {
			for (var iaestack : storage) {
				if (iaestack.getStackSize() != 0 && this.canHandleStack(iaestack)) {
					this.cache.add(iaestack);
				}
			}
		}
	}

	/**
	 * Returns whether the handler can handle the given stack.
	 *
	 * @param stack Stack to check
	 * @return Whether the handler can handle the stack
	 */
	protected boolean canHandleStack(T stack) {
		return true;
	}

	/**
	 * Pulls a stack from the network.
	 *
	 * @param stack    Stack to pull
	 * @param simulate Whether to simulate the pull
	 * @return Pulled stack, or null if the stack could not be pulled
	 */
	@Nullable
	protected T pullStackInternal(T stack, boolean simulate) {
		if (!this.cache.isEmpty()) {
			try {
				return Platform.poweredExtraction(this.getProxy().getEnergy(),
					this.getMonitor(),
					stack,
					this.getActionSource(),
					simulate ? Actionable.SIMULATE : Actionable.MODULATE);
			} catch (GridAccessException ignored) {}
		}

		return null;
	}

	/**
	 * Pulls a stack from the network.
	 *
	 * @param stack Stack to pull
	 * @return Pulled stack, or null if the stack could not be pulled
	 */
	@Nullable
	protected T pullStack(T stack, boolean simulate) {
		if (this.lockRecursion()) {
			return null;
		}

		this.updateMonitor();

		try {
			return this.pullStackInternal(stack, simulate);
		} finally {
			this.unlockRecursion();
		}
	}

	/**
	 * Pulls a stack from the network, then bubbles the slot up for faster subsequent lookups.
	 *
	 * @param slot     Slot to pull from
	 * @param maxAmt   Maximum amount to pull
	 * @param simulate Whether to simulate the pull
	 * @return Pulled stack, or null if the stack could not be pulled
	 */
	@Nullable
	protected T pullStackFromSlot(int slot, long maxAmt, boolean simulate) {
		if (this.lockRecursion()) {
			return null;
		}

		try {
			this.updateMonitor();

			if (slot < this.cache.size()) {
				var stack = this.cache.getByIndex(slot);
				if (stack != null) {
					if (slot > 0 && !simulate) {
						this.cache.makeFirst(stack);
					}

					return this.pullStackInternal(stack.copy().setStackSize(maxAmt), simulate);
				}
			}

			return null;
		} finally {
			this.unlockRecursion();
		}
	}

	/**
	 * React to network changes.
	 */
	@Override
	public void postChange(IBaseMonitor<T> iBaseMonitor, Iterable<T> iterable, IActionSource iActionSource) {
		var storage = this.getStorageList();
		if (storage == null) return;

		for (var stack : iterable) {
			this.cache.remove(stack);

			var stackInNetwork = storage.findPrecise(stack);
			if (stackInNetwork != null && stackInNetwork.getStackSize() > 0 && this.canHandleStack(stackInNetwork)) {
				this.cache.add(stackInNetwork);
			}
		}
	}

	/**
	 * React to network changes.
	 */
	@Override
	public void onListUpdate() {
		this.refreshCache();
	}

	/**
	 * React to network changes.
	 *
	 * @param oldValue Old monitor
	 * @param newValue New monitor
	 */
	@Override
	protected void onMonitorChange(IMEMonitor<T> oldValue, IMEMonitor<T> newValue) {
		this.refreshCache();
	}

	/**
	 * Returns the current cache.
	 *
	 * @return Current cache
	 */
	public @NotNull ObjectIndexableLinkedOpenHashSet<T> getCache() {
		return this.cache;
	}

	/**
	 * Returns the stack in the given slot, or null if the slot is empty.
	 *
	 * @param slot Slot to get stack from
	 * @return Stack in the slot, or null if the slot is empty
	 */
	@Nullable
	protected T getInSlot(int slot) {
		if (this.lockRecursion()) {
			return null;
		}

		try {
			this.updateMonitor();

			if (!this.cache.isEmpty() && slot < this.cache.size()) {
				return this.cache.getByIndex(slot);
			}

			return null;
		} finally {
			this.unlockRecursion();
		}
	}
}
