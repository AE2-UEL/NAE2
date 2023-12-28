package co.neeve.nae2.common.helpers.exposer;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import co.neeve.nae2.common.interfaces.IExposerHost;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.Nullable;

public class FluidExposerHandler extends ExposerHandler<IAEFluidStack> implements IFluidHandler {
	private static final IFluidTankProperties[] EMPTY = new IFluidTankProperties[]{};
	private ObjectArrayList<IAEFluidStack> fluidCache;
	private IFluidTankProperties[] cachedProperties;

	public FluidExposerHandler(IExposerHost host) {
		super(host);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		this.updateMonitor();
		return this.cachedProperties != null ? this.cachedProperties : EMPTY;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		this.updateMonitor();

		var storage = this.getStorage();
		if (storage != null) {
			var aefs = AEFluidStack.fromFluidStack(resource);
			var stack = storage.findPrecise(aefs);
			if (stack != null) {
				try {
					var extracted = Platform.poweredExtraction(this.host.getProxy().getEnergy(),
						this.host.getProxy().getStorage().getInventory(this.getStorageChannel()),
						stack, this.mySrc, doDrain ? Actionable.MODULATE : Actionable.SIMULATE);
					if (extracted != null) {
						return extracted.getFluidStack();
					}
				} catch (GridAccessException ignored) {}
			}
		}

		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		this.updateMonitor();

		if (this.fluidCache != null && !this.fluidCache.isEmpty()) {
			var stack = this.fluidCache.stream().findFirst();
			try {
				var extracted = Platform.poweredExtraction(this.host.getProxy().getEnergy(),
					this.host.getProxy().getStorage().getInventory(this.getStorageChannel()),
					stack.get(), this.mySrc, doDrain ? Actionable.MODULATE : Actionable.SIMULATE);
				if (extracted != null) {
					return extracted.getFluidStack();
				}
			} catch (GridAccessException ignored) {}
		}

		return null;
	}

	@Override
	protected void onMonitorChange(IMEMonitor<IAEFluidStack> oldValue, IMEMonitor<IAEFluidStack> newValue) {
		this.updateFluidCache();
	}

	private void updateFluidCache() {
		if (this.monitor == null) {
			this.fluidCache = null;
			this.cachedProperties = null;
		} else {
			this.fluidCache = new ObjectArrayList<>();
			var storage = this.getStorage();
			if (storage != null) {
				for (var iaeItemStack : storage) {
					this.fluidCache.add(iaeItemStack);
				}
			}

			this.cachedProperties = new IFluidTankProperties[this.fluidCache.size()];
			var i = 0;
			for (var cachedFluid : this.fluidCache) {
				this.cachedProperties[i++] = new FluidTankProperties(cachedFluid.getFluidStack(),
					(int) cachedFluid.getStackSize(), false, true);
			}
		}
	}

	@Override
	protected IStorageChannel<IAEFluidStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> iBaseMonitor, Iterable<IAEFluidStack> iterable,
	                       IActionSource iActionSource) {
		this.updateFluidCache();
	}

	@Override
	public void onListUpdate() {
		this.updateFluidCache();
	}
}
