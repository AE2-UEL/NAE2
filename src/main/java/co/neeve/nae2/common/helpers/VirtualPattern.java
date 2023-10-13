package co.neeve.nae2.common.helpers;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import com.google.common.collect.Iterables;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class VirtualPattern implements ICraftingPatternDetails {
	private final IAEItemStack[] outputs;
	private final IAEItemStack[] inputs;
	private int priority = 0;

	public VirtualPattern(Iterable<IAEItemStack> inputs, Iterable<IAEItemStack> outputs) {
		this.inputs = Iterables.toArray(inputs, IAEItemStack.class);
		this.outputs = Iterables.toArray(outputs, IAEItemStack.class);
	}

	@Override
	public ItemStack getPattern() {
		return null;
	}

	@Override
	public boolean isValidItemForSlot(int i, ItemStack itemStack, World world) {
		return false;
	}

	@Override
	public boolean isCraftable() {
		return false;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return inputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return inputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return outputs;
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return outputs;
	}

	@Override
	public boolean canSubstitute() {
		return false;
	}

	@Override
	public ItemStack getOutput(InventoryCrafting inventoryCrafting, World world) {
		return null;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public void setPriority(int i) {
		this.priority = i;
	}
}
