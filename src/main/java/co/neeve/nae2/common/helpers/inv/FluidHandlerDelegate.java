package co.neeve.nae2.common.helpers.inv;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class FluidHandlerDelegate implements IFluidHandler, IDelegate<IFluidHandler> {
	public static final IFluidTankProperties[] NONE = new IFluidTankProperties[0];
	@Nullable
	protected IFluidHandler delegate;

	public FluidHandlerDelegate(@Nullable IFluidHandler delegate) {
		this.delegate = delegate;
	}

	public FluidHandlerDelegate() {
		this(null);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		if (this.delegate == null) {
			return NONE;
		}
		return this.delegate.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (this.delegate == null) {
			return 0;
		}
		return this.delegate.fill(resource, doFill);
	}

	@Override
	@Nullable
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (this.delegate == null) {
			return null;
		}
		return this.delegate.drain(resource, doDrain);
	}

	@Override
	@Nullable
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (this.delegate == null) {
			return null;
		}
		return this.delegate.drain(maxDrain, doDrain);
	}

	@Nullable
	public IFluidHandler getDelegate() {
		return this.delegate;
	}

	public void setDelegate(@Nullable IFluidHandler delegate) {
		this.delegate = delegate;
	}
}
