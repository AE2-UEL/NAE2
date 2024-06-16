package co.neeve.nae2.common.parts.p2p.iface;

import appeng.util.inv.WrapperChainedItemHandler;
import co.neeve.nae2.common.helpers.inv.WrapperChainedFluidHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CapabilityContainer implements IItemHandler, IFluidHandler, ICapabilityProvider {
	public static final IFluidTankProperties[] NONE = new IFluidTankProperties[0];
	protected IItemHandler itemHandler = EmptyHandler.INSTANCE;
	protected IFluidHandler fluidHandler = EmptyFluidHandler.INSTANCE;
	protected boolean isViewing = false;

	@Override
	public IFluidTankProperties[] getTankProperties() {
		if (this.isViewing) return NONE;
		this.isViewing = true;

		try {
			return this.fluidHandler.getTankProperties();
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (this.isViewing) return 0;
		this.isViewing = true;

		try {
			return this.fluidHandler.fill(resource, doFill);
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	@Nullable
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (this.isViewing) return null;
		this.isViewing = true;

		try {
			return this.fluidHandler.drain(resource, doDrain);
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	@Nullable
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (this.isViewing) return null;
		this.isViewing = true;

		try {
			return this.fluidHandler.drain(maxDrain, doDrain);
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	public int getSlots() {
		if (this.isViewing) return 0;
		this.isViewing = true;

		try {
			return this.itemHandler.getSlots();
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		if (this.isViewing) return ItemStack.EMPTY;
		this.isViewing = true;

		try {
			return this.itemHandler.getStackInSlot(slot);
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (this.isViewing) return ItemStack.EMPTY;
		this.isViewing = true;

		try {
			return this.itemHandler.insertItem(slot,
				stack,
				simulate);
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (this.isViewing) return ItemStack.EMPTY;
		this.isViewing = true;

		try {
			return this.itemHandler.extractItem(slot,
				amount,
				simulate);
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		if (this.isViewing) return 0;
		this.isViewing = true;

		try {
			return this.itemHandler.getSlotLimit(slot);
		} finally {
			this.isViewing = false;
		}
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		if (this.isViewing) return false;
		this.isViewing = true;

		try {
			return this.itemHandler.isItemValid(slot, stack);
		} finally {
			this.isViewing = false;
		}
	}

	public IItemHandler getItemHandler() {
		return this.itemHandler;
	}

	public void setItemHandler(List<IItemHandler> itemHandler) {
		this.setItemHandler(itemHandler.toArray(new IItemHandler[0]));
	}

	public void setItemHandler(IItemHandler[] itemHandlers) {
		this.itemHandler = new WrapperChainedItemHandler(itemHandlers);
	}

	public void setFluidHandler(List<IFluidHandler> fluidHandler) {
		this.setFluidHandler(fluidHandler.toArray(new IFluidHandler[0]));
	}

	public void setFluidHandler(IFluidHandler[] fluidHandlers) {
		this.fluidHandler = new WrapperChainedFluidHandler(fluidHandlers);
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability,
	                             @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
			|| capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getCapability(@NotNull Capability<T> capability,
	                           @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) this.itemHandler;
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this.fluidHandler;
		return null;
	}
}