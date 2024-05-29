package co.neeve.nae2.common.crafting.patterntransform;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import co.neeve.nae2.common.crafting.patterntransform.transformers.IPatternTransformer;
import co.neeve.nae2.common.items.NAEBaseItemUpgrade;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatternTransform {
	protected final static List<IPatternTransformer> transformers = new ArrayList<>();

	public static void registerTransformer(IPatternTransformer transformer) {
		transformers.add(transformer);
	}

	public static ICraftingPatternDetails transform(ICraftingMedium medium, ICraftingPatternDetails pattern) {
		final var originalInputs = pattern.getInputs();
		final var originalOutputs = pattern.getOutputs();
		var inputs = originalInputs;
		var outputs = originalOutputs;

		for (var transformer : transformers) {
			if (!transformer.shouldTransform(medium, pattern)) continue;

			inputs = transformer.transformInputs(medium, pattern, inputs);
			outputs = transformer.transformOutputs(medium, pattern, outputs);
		}

		if (!Arrays.equals(inputs, originalInputs) || !Arrays.equals(outputs, originalOutputs)) {
			return new PatternTransformWrapper(pattern, inputs, outputs);
		}
		return pattern;
	}

	@Unique
	public static boolean isTransformer(ItemStack is) {
		return is.getItem() instanceof NAEBaseItemUpgrade upgrade &&
			upgrade.getType(is) == Upgrades.UpgradeType.GREGTECH_CIRCUIT;
	}
}
