package co.neeve.nae2.common.helpers;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class WrapperChainedFluidHandler implements IFluidHandler {
	private final IFluidHandler[] handlers;
	private IFluidTankProperties[] cachedProperties;
	private int depth;

	public WrapperChainedFluidHandler(IFluidHandler[] array) {
		this.handlers = array;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		if (this.depth > 0) return new IFluidTankProperties[0];
		if (this.cachedProperties != null) return this.cachedProperties;

		this.depth++;
		var properties = new ArrayList<IFluidTankProperties>();
		for (var tank : this.handlers) {
			properties.addAll(Arrays.asList(tank.getTankProperties()));
		}
		this.depth--;

		this.cachedProperties = properties.toArray(new IFluidTankProperties[0]);
		return this.cachedProperties;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (this.depth == 1) return 0;
		this.depth++;

		var used = 0;
		for (var handler : this.handlers) {
			var fs = resource.copy();
			fs.amount -= used;
			if (fs.amount <= 0) break;
			used += handler.fill(fs, doFill);
		}

		this.depth--;
		return used;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack fluid, boolean doDrain) {
		if (this.depth > 0) return null;
		this.depth++;

		if (fluid.amount > 0) {
			var resource = fluid.copy();
			FluidStack totalDrained = null;

			for (var handler : this.handlers) {
				var drain = handler.drain(resource, doDrain);
				if (drain != null) {
					if (totalDrained == null) {
						totalDrained = drain;
					} else {
						totalDrained.amount += drain.amount;
					}

					resource.amount -= drain.amount;
					if (resource.amount <= 0) {
						break;
					}
				}
			}

			this.depth--;
			return totalDrained;
		} else {
			this.depth--;
			return null;
		}
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (this.depth > 0) return null;
		this.depth++;

		if (maxDrain == 0) {
			this.depth--;
			return null;
		} else {
			FluidStack totalDrained = null;
			var toDrain = maxDrain;

			for (var fh : this.handlers) {
				if (totalDrained == null) {
					totalDrained = fh.drain(toDrain, doDrain);
					if (totalDrained != null) {
						toDrain -= totalDrained.amount;
					}
				} else {
					var copy = totalDrained.copy();
					copy.amount = toDrain;
					var drain = fh.drain(copy, doDrain);
					if (drain != null) {
						totalDrained.amount += drain.amount;
						toDrain -= drain.amount;
					}
				}

				if (toDrain <= 0) {
					break;
				}
			}


			this.depth--;
			return totalDrained;
		}
	}
}
