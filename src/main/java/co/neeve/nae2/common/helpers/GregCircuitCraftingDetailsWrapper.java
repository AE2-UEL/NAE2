package co.neeve.nae2.common.helpers;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class GregCircuitCraftingDetailsWrapper extends CraftingDetailsWrapper {
	protected final int config;
	protected IAEItemStack[] oldInputs;
	protected IAEItemStack[] oldCondInputs;
	protected IAEItemStack[] cachedInputs;
	protected IAEItemStack[] cachecCondInputs;

	public GregCircuitCraftingDetailsWrapper(ICraftingPatternDetails delegate, int config) {
		super(delegate);
		this.config = config;
	}

	@NotNull
	protected static IAEItemStack[] filterCircuitsOut(IAEItemStack[] inputs) {
		if (inputs == null) return null;

		return Arrays.stream(inputs)
			.filter(Objects::nonNull)
			.filter(x -> !IntCircuitIngredient.isIntegratedCircuit(x.createItemStack()))
			.toArray(IAEItemStack[]::new);
	}

	public int getConfig() {
		return this.config;
	}

	@Override
	public IAEItemStack[] getInputs() {
		var inputs = super.getInputs();
		if (inputs != this.oldInputs) {
			this.oldInputs = inputs;
			this.cachedInputs = filterCircuitsOut(inputs);
		}
		return this.cachedInputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		var inputs = super.getCondensedInputs();
		if (inputs != this.oldCondInputs) {
			this.oldCondInputs = inputs;
			this.cachecCondInputs = filterCircuitsOut(inputs);
		}
		return this.cachecCondInputs;
	}
}
