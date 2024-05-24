package co.neeve.nae2.common.crafting.patterntransform.transformers;

import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import co.neeve.nae2.common.interfaces.IExtendedUpgradeInventory;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class GregTechCircuitPatternTransformer implements IPatternTransformer {
	public static Optional<Integer> getCircuitValueFromDetails(ICraftingPatternDetails details) {
		var optCircuit = Arrays.stream(details.getInputs())
			.filter(Objects::nonNull)
			.filter(ais -> IntCircuitIngredient.isIntegratedCircuit(ais.createItemStack()))
			.findFirst();

		if (!optCircuit.isPresent()) return Optional.empty();

		var circuit = optCircuit.get();
		var config = IntCircuitIngredient.getCircuitConfiguration(circuit.createItemStack());
		return Optional.of(config);
	}

	@NotNull
	protected static IAEItemStack[] filterCircuitsOut(IAEItemStack[] inputs) {
		if (inputs == null) return null;

		return Arrays.stream(inputs)
			.filter(Objects::nonNull)
			.filter(x -> !IntCircuitIngredient.isIntegratedCircuit(x.createItemStack()))
			.toArray(IAEItemStack[]::new);
	}

	@Override
	public boolean shouldTransform(ICraftingMedium medium, ICraftingPatternDetails details) {
		if (details.isCraftable()) return false;

		var optCircuit = getCircuitValueFromDetails(details);
		if (!optCircuit.isPresent()) return false;

		return medium instanceof IUpgradeableHost upgradeableHost
			&& upgradeableHost.getInventoryByName("upgrades") instanceof IExtendedUpgradeInventory naeUpgrades
			&& naeUpgrades.getInstalledUpgrades(Upgrades.UpgradeType.GREGTECH_CIRCUIT) > 0;
	}

	@Override
	public IAEItemStack[] transformInputs(ICraftingMedium medium, ICraftingPatternDetails details,
	                                      IAEItemStack[] inputs) {
		return filterCircuitsOut(inputs);
	}
}
