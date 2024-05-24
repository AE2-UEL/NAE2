package co.neeve.nae2.common.crafting.patterntransform.transformers;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public interface IPatternTransformer {
	default IAEItemStack[] transformInputs(ICraftingMedium medium, ICraftingPatternDetails details,
	                                       IAEItemStack[] inputs) {
		return inputs;
	}

	default IAEItemStack[] transformOutputs(ICraftingMedium medium, ICraftingPatternDetails details,
	                                        IAEItemStack[] outputs) {
		return outputs;
	}

	boolean shouldTransform(ICraftingMedium medium, ICraftingPatternDetails details);
}
