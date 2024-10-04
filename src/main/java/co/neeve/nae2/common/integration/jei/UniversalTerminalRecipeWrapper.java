package co.neeve.nae2.common.integration.jei;

import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.helpers.UniversalTerminalHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UniversalTerminalRecipeWrapper implements IRecipeWrapper {
    public UniversalTerminalRecipeWrapper(boolean isUniversalTerminal) {
        this.isUniversalTerminal = isUniversalTerminal;
    }

    private final boolean isUniversalTerminal;


    @Override
    public void getIngredients(IIngredients iIngredients) {
        List<List<ItemStack>> inputList = new ArrayList<>();

        if (isUniversalTerminal) {
            List<ItemStack> input = new ArrayList<>();
            ItemStack itemStack = NAE2.definitions().items().universalWirelessTerminal().maybeStack(1).orElse(null);
            if (itemStack != ItemStack.EMPTY) {
                input.add(itemStack);
            }

            inputList.add(input);
        } else {
            inputList.add(UniversalTerminalHelper.wirelessTerminals);
        }

        inputList.add(UniversalTerminalHelper.terminals);

        iIngredients.setInputLists(VanillaTypes.ITEM, inputList);
        if (NAE2.definitions().items().universalWirelessTerminal().maybeStack(1).isPresent()){
            iIngredients.setOutput(VanillaTypes.ITEM,NAE2.definitions().items().universalWirelessTerminal().maybeStack(1).get());
        }
    }
}
