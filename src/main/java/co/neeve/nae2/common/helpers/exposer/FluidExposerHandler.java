package co.neeve.nae2.common.helpers.exposer;

import appeng.api.AEApi;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import co.neeve.nae2.common.interfaces.IExposerHost;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.Nullable;

public class FluidExposerHandler extends AEStackExposerHandler<IAEFluidStack> implements IFluidHandler {
	private static final IFluidTankProperties[] EMPTY = new IFluidTankProperties[]{};
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

		var storage = this.getStorageList();
		if (storage != null) {
			var aefs = AEFluidStack.fromFluidStack(resource);
			var stack = storage.findPrecise(aefs);
			if (stack != null) {
				var pulled = this.pullStack(aefs, !doDrain);
				if (pulled != null) {
					return pulled.getFluidStack();
				}
			}
		}

		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		this.updateMonitor();

		if (!this.cache.isEmpty()) {
			var stack = this.cache.stream().findFirst();
			var pulled = this.pullStack(stack.get().copy().setStackSize(maxDrain), !doDrain);
			if (pulled != null) {
				return pulled.getFluidStack();
			}
		}

		return null;
	}

	@Override
	protected IStorageChannel<IAEFluidStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
	}

	@Override
	protected void refreshCache() {
		super.refreshCache();

		this.rebuildProperties();
	}

	private void rebuildProperties() {
		if (this.cache.isEmpty()) {
			this.cachedProperties = null;
		} else {
			this.cachedProperties = new IFluidTankProperties[this.cache.size()];
			var i = 0;
			for (var cachedFluid : this.cache) {
				this.cachedProperties[i++] = new FluidTankProperties(cachedFluid.getFluidStack(),
					(int) cachedFluid.getStackSize(), false, true);
			}
		}
	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> iBaseMonitor, Iterable<IAEFluidStack> iterable,
	                       IActionSource iActionSource) {
		super.postChange(iBaseMonitor, iterable, iActionSource);

		// pretty stupid that I have to rebuild this, since we're expected to provide an array.
		this.rebuildProperties();
	}
}
