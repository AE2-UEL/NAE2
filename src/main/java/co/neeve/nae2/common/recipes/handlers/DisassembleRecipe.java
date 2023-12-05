package co.neeve.nae2.common.recipes.handlers;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import co.neeve.nae2.NAE2;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DisassembleRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	private static final ItemStack MISMATCHED_STACK = ItemStack.EMPTY;

	private final Map<IItemDefinition, IItemDefinition> cellMappings;
	private final Map<IItemDefinition, IItemDefinition> nonCellMappings;

	public DisassembleRecipe() {
		final var definitions = NAE2.definitions();
		final var blocks = definitions.blocks();
		final var items = definitions.items();
		final var mats = definitions.materials();

		this.cellMappings = new HashMap<>(6);
		this.nonCellMappings = new HashMap<>(4);

		this.cellMappings.put(items.storageCellVoid(), mats.cellPartVoid());
		this.cellMappings.put(items.fluidStorageCellVoid(), mats.cellPartVoid());
		this.cellMappings.put(items.storageCell256K(), mats.cellPart256K());
		this.cellMappings.put(items.storageCell1024K(), mats.cellPart1024K());
		this.cellMappings.put(items.storageCell4096K(), mats.cellPart4096K());
		this.cellMappings.put(items.storageCell16384K(), mats.cellPart16384K());
		this.cellMappings.put(items.storageCellFluid256K(), mats.cellPart256K());
		this.cellMappings.put(items.storageCellFluid1024K(), mats.cellPart1024K());
		this.cellMappings.put(items.storageCellFluid4096K(), mats.cellPart4096K());
		this.cellMappings.put(items.storageCellFluid16384K(), mats.cellPart16384K());

		this.nonCellMappings.put(blocks.storageCrafting256K(), mats.cellPart256K());
		this.nonCellMappings.put(blocks.storageCrafting1024K(), mats.cellPart1024K());
		this.nonCellMappings.put(blocks.storageCrafting4096K(), mats.cellPart4096K());
		this.nonCellMappings.put(blocks.storageCrafting16384K(), mats.cellPart16384K());
	}

	@Override
	public boolean matches(final @NotNull InventoryCrafting inv, final @NotNull World w) {
		var output = this.getOutput(inv);
		return output != null && !output.isEmpty();
	}

	private ItemStack getOutput(final IInventory inventory) {
		var itemCount = 0;
		var output = MISMATCHED_STACK;

		for (var slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++) {
			final var stackInSlot = inventory.getStackInSlot(slotIndex);
			if (!stackInSlot.isEmpty()) {
				// needs a single input in the recipe
				itemCount++;
				if (itemCount > 1) {
					return MISMATCHED_STACK;
				}

				// handle storage cells
				var maybeCellOutput = this.getCellOutput(stackInSlot);
				if (maybeCellOutput.isPresent()) {
					var storageCellStack = maybeCellOutput.get();
					// make sure the storage cell stackInSlot empty...
					final IMEInventory<IAEItemStack> cellInv = AEApi.instance()
						.registries()
						.cell()
						.getCellInventory(stackInSlot, null,
							AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
					if (cellInv != null) {
						final var list = cellInv
							.getAvailableItems(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
						if (!list.isEmpty()) {
							return ItemStack.EMPTY;
						}
					}

					output = storageCellStack;
				}

				// handle crafting storage blocks
				output = this.getNonCellOutput(stackInSlot).orElse(output);
			}
		}

		return output;
	}

	@Nonnull
	private Optional<ItemStack> getCellOutput(final ItemStack compared) {
		for (final var entry : this.cellMappings.entrySet()) {
			if (entry.getKey().isSameAs(compared)) {
				return entry.getValue().maybeStack(1);
			}
		}

		return Optional.empty();
	}

	@Nonnull
	private Optional<ItemStack> getNonCellOutput(final ItemStack compared) {
		for (final var entry : this.nonCellMappings.entrySet()) {
			if (entry.getKey().isSameAs(compared)) {
				return entry.getValue().maybeStack(1);
			}
		}

		return Optional.empty();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(final @NotNull InventoryCrafting inv) {
		return this.getOutput(inv);
	}

	@Override
	public boolean canFit(int i, int i1) {
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() // no default output..
	{
		return ItemStack.EMPTY;
	}
}