package co.neeve.nae2.common.helpers;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class CraftingDetailsWrapper implements ICraftingPatternDetails {
	protected final ICraftingPatternDetails delegate;

	public CraftingDetailsWrapper(ICraftingPatternDetails delegate) {
		this.delegate = delegate;
	}

	@Override
	public ItemStack getPattern() {
		return this.delegate.getPattern();
	}

	@Override
	public boolean isValidItemForSlot(int i, ItemStack itemStack, World world) {
		return this.delegate.isValidItemForSlot(i, itemStack, world);
	}

	@Override
	public boolean isCraftable() {
		return this.delegate.isCraftable();
	}

	@Override
	public IAEItemStack[] getInputs() {
		return this.delegate.getInputs();
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return this.delegate.getCondensedInputs();
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return this.delegate.getCondensedOutputs();
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return this.delegate.getOutputs();
	}

	@Override
	public boolean canSubstitute() {
		return this.delegate.canSubstitute();
	}

	@Override
	public ItemStack getOutput(InventoryCrafting inventoryCrafting, World world) {
		return this.delegate.getOutput(inventoryCrafting, world);
	}

	@Override
	public int getPriority() {
		return this.delegate.getPriority();
	}

	@Override
	public void setPriority(int i) {
		this.delegate.setPriority(i);
	}
}
