package co.neeve.nae2.common.recipes.handlers;

import co.neeve.nae2.common.items.cells.vc.VoidCell;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class VoidConversionRecipe extends ShapelessRecipes {
	private final ItemStack from;
	private final ItemStack to;

	public VoidConversionRecipe(ItemStack from, ItemStack to) {
		super("", to, NonNullList.create());
		this.getIngredients().add(Ingredient.fromStacks(from));
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean matches(@NotNull InventoryCrafting inv, @NotNull World worldIn) {
		return !this.getOutput(inv).isEmpty();
	}

	@Override
	public @NotNull ItemStack getCraftingResult(@NotNull InventoryCrafting inv) {
		return this.getOutput(inv);
	}

	public ItemStack getOutput(InventoryCrafting inv) {
		var output = ItemStack.EMPTY;
		for (var i = 0; i < inv.getSizeInventory(); i++) {
			var stack = inv.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (stack.isItemEqual(this.from)) {
				output = this.to.copy();
				// Copy power over.
				((VoidCell<?>) output.getItem())
					.setCondenserPower(output, ((VoidCell<?>) stack.getItem()).getCondenserPower(stack));
				continue;
			}

			if (!output.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		return output;
	}

	@Override
	public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}
}
