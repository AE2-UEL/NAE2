package co.neeve.nae2.common.helpers;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.NAE2;
import com.google.common.collect.Iterables;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Objects;

public class VirtualPatternDetails implements ICraftingPatternDetails {
	private final IAEItemStack[] outputs;
	private final IAEItemStack[] inputs;
	private int priority = 0;

	public VirtualPatternDetails(Iterable<IAEItemStack> inputs, Iterable<IAEItemStack> outputs) {
		this.inputs = Iterables.toArray(inputs, IAEItemStack.class);
		this.outputs = Iterables.toArray(outputs, IAEItemStack.class);
	}

	public static VirtualPatternDetails fromItemStack(ItemStack is) {
		var inputItems = new ArrayList<IAEItemStack>();
		var outputItems = new ArrayList<IAEItemStack>();

		try {
			var compound = is.getTagCompound();
			for (var input : Objects.requireNonNull(compound).getTagList("inputs", Constants.NBT.TAG_COMPOUND)) {
				inputItems.add(AEItemStack.fromNBT((NBTTagCompound) input));
			}

			for (var output : Objects.requireNonNull(compound).getTagList("outputs", Constants.NBT.TAG_COMPOUND)) {
				outputItems.add(AEItemStack.fromNBT((NBTTagCompound) output));
			}
		} catch (NullPointerException npe) {
			throw new IllegalStateException("No pattern here!");
		}

		if (outputItems.isEmpty()) {
			throw new IllegalStateException("No pattern here!");
		}

		return new VirtualPatternDetails(inputItems, outputItems);
	}

	@Override
	public ItemStack getPattern() {
		var is = Objects.requireNonNull(NAE2.definitions().items().virtualPattern().maybeStack(1).orElse(null));
		var compound = new NBTTagCompound();
		var tagInputs = new NBTTagList();
		for (var input : this.inputs) {
			var itemcmpd = new NBTTagCompound();
			input.writeToNBT(itemcmpd);
			tagInputs.appendTag(itemcmpd);
		}
		compound.setTag("inputs", tagInputs);

		var tagOutputs = new NBTTagList();
		for (var output : this.outputs) {
			var itemcmpd = new NBTTagCompound();
			output.writeToNBT(itemcmpd);
			tagOutputs.appendTag(itemcmpd);
		}
		compound.setTag("outputs", tagOutputs);

		is.setTagCompound(compound);
		return is;
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
		return this.inputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return this.inputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return this.outputs;
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return this.outputs;
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
