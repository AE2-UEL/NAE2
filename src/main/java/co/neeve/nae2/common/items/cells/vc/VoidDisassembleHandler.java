package co.neeve.nae2.common.items.cells.vc;

import co.neeve.nae2.common.registries.Materials;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

public class VoidDisassembleHandler extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	public VoidDisassembleHandler() {

	}

	@Override
	public boolean matches(@NotNull InventoryCrafting inv, @NotNull World worldIn) {
		var matches = false;
		int itemCount = 0;

		for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); slotIndex++) {
			final ItemStack stackInSlot = inv.getStackInSlot(slotIndex);
			if (!stackInSlot.isEmpty()) {
				// needs a single input in the recipe
				itemCount++;
				if (itemCount > 1) return false;

				if (stackInSlot.getItem() instanceof BaseStorageCellVoid<?> cell && cell.getCondenserPower(stackInSlot) < 1) {
					matches = true;
				} else {
					return false;
				}
			}
		}

		return matches;
	}

	@Override
	public @NotNull ItemStack getCraftingResult(@NotNull InventoryCrafting inv) {
		return Materials.CELL_VOID_PART.getStack();
	}

	@Override
	public boolean canFit(int width, int height) {
		return width >= 1 && height >= 1;
	}

	@Override
	public @NotNull ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}
}
