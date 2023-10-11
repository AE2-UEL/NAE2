package co.neeve.nae2.common.items.cells.vc;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class VoidCraftingHandler extends ShapedRecipes {
	private final ItemStack left;
	private final ItemStack right;

	public VoidCraftingHandler(ItemStack left, ItemStack right, ItemStack result) {
		super("", 2, 1, NonNullList.create(), result);
		this.getIngredients().add(Ingredient.fromStacks(left));
		this.getIngredients().add(Ingredient.fromStacks(right));
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean matches(@NotNull InventoryCrafting inv, @NotNull World worldIn) {
		var width = inv.getWidth();
		for (int slotIndex = 0; slotIndex < inv.getSizeInventory() - 1; slotIndex++) {
			final ItemStack stackInSlot = inv.getStackInSlot(slotIndex);
			if (!stackInSlot.isEmpty()) {
				var rightStack = (slotIndex + 1) / width == (slotIndex / width)
					? inv.getStackInSlot(slotIndex + 1) : null;
				return rightStack != null && ItemStack.areItemStacksEqual(stackInSlot, left) && ItemStack.areItemStacksEqual(rightStack, right);
			}
		}
		return false;
	}
}
