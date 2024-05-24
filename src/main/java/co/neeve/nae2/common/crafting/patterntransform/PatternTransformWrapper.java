package co.neeve.nae2.common.crafting.patterntransform;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PatternTransformWrapper implements ICraftingPatternDetails {
	private final IAEItemStack[] condensedInputs;
	private final IAEItemStack[] condensedOutputs;
	private final ICraftingPatternDetails delegate;
	private final IAEItemStack[] inputs;
	private final IAEItemStack[] outputs;

	public PatternTransformWrapper(ICraftingPatternDetails delegate, IAEItemStack[] inputs, IAEItemStack[] outputs) {
		this.delegate = delegate;
		this.inputs = inputs;
		this.outputs = outputs;

		this.condensedInputs = new IAEItemStack[inputs.length];
		var offset = 0;

		IAEItemStack io;
		Iterator<IAEItemStack> iterator;
		for (iterator = Arrays.stream(inputs).iterator(); iterator.hasNext(); ++offset) {
			io = iterator.next();
			this.condensedInputs[offset] = io;
		}

		offset = 0;
		this.condensedOutputs = new IAEItemStack[outputs.length];

		for (iterator = Arrays.stream(outputs).iterator(); iterator.hasNext(); ++offset) {
			io = iterator.next();
			this.condensedOutputs[offset] = io;
		}
	}

	@Override
	public ItemStack getPattern() {return this.delegate.getPattern();}

	@Override
	public boolean isValidItemForSlot(int i, ItemStack itemStack, World world) {
		return this.delegate.isValidItemForSlot(i,
			itemStack,
			world);
	}

	@Override
	public boolean isCraftable() {return this.delegate.isCraftable();}

	@Override
	public boolean canSubstitute() {return this.delegate.canSubstitute();}

	@Override
	public List<IAEItemStack> getSubstituteInputs(int slot) {return this.delegate.getSubstituteInputs(slot);}

	@Override
	public ItemStack getOutput(InventoryCrafting inventoryCrafting, World world) {
		return this.delegate.getOutput(inventoryCrafting,
			world);
	}

	@Override
	public int getPriority() {return this.delegate.getPriority();}

	@Override
	public void setPriority(int i) {this.delegate.setPriority(i);}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return this.condensedInputs;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return this.inputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return this.condensedOutputs;
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return this.outputs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PatternTransformWrapper that)) return false;
		return Arrays.equals(this.condensedInputs, that.condensedInputs) && Arrays.equals(this.condensedOutputs,
			that.condensedOutputs) && Objects.equals(this.delegate, that.delegate) && Arrays.equals(this.inputs,
			that.inputs) && Arrays.equals(this.outputs, that.outputs);
	}

	@Override
	public int hashCode() {
		var result = Objects.hash(this.delegate);
		result = 31 * result + Arrays.hashCode(this.condensedInputs);
		result = 31 * result + Arrays.hashCode(this.condensedOutputs);
		result = 31 * result + Arrays.hashCode(this.inputs);
		result = 31 * result + Arrays.hashCode(this.outputs);
		return result;
	}

	public ICraftingPatternDetails getDelegate() {
		return this.delegate;
	}
}
