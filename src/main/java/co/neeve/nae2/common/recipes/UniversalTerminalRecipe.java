package co.neeve.nae2.common.recipes;

import appeng.api.config.Actionable;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.util.Platform;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.api.config.WirelessTerminalType;
import co.neeve.nae2.common.helpers.UniversalTerminalHelper;
import co.neeve.nae2.common.items.WirelessTerminalUniversal;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class UniversalTerminalRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasWireless = false;
        boolean isUniversal = false;
        boolean hasTerminal = false;
        EnumSet<WirelessTerminalType> terminals = EnumSet.noneOf(WirelessTerminalType.class);
        ItemStack terminal = ItemStack.EMPTY;
        int size = inv.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item == NAE2.definitions().items().universalWirelessTerminal().maybeItem().orElse(null)) {
                    hasWireless = true;
                    isUniversal = true;
                    terminal = stack;
                }

                if (UniversalTerminalHelper.isWirelessTerminal(stack)) {
                    if (hasWireless) {
                        return false;
                    }
                    hasWireless = true;
                    terminal = stack;
                } else if (UniversalTerminalHelper.isTerminal(stack)) {
                    hasTerminal = true;

                    WirelessTerminalType terminalType = UniversalTerminalHelper.getTerminalType(stack);

                    if (terminals.contains(terminalType)) {
                        return false;
                    }

                    if (terminalType != null) {
                        terminals.add(terminalType);
                    }
                }
            }
        }

        if (!(hasTerminal && hasWireless)) {
            return false;
        }

        if (isUniversal) {
            for (WirelessTerminalType terminalType : terminals) {
                if (UniversalTerminalHelper.isInstalled(terminal, terminalType)) {
                    return false;
                }
            }

            return true;
        }

        WirelessTerminalType terminalType = UniversalTerminalHelper.getTerminalType(terminal);

        for (WirelessTerminalType wirelessTerminalType : terminals) {
            if (wirelessTerminalType == terminalType) {
                return false;
            }
        }

        return true;

    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        boolean isUniversal = false;
        EnumSet<WirelessTerminalType> terminals = EnumSet.noneOf(WirelessTerminalType.class);
        ItemStack terminal = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                Item universalTerminalItem = NAE2.definitions().items().universalWirelessTerminal().maybeItem().orElse(null);
                if (item == universalTerminalItem) {
                    isUniversal = true;
                    terminal = stack.copy();
                } else if (UniversalTerminalHelper.isWirelessTerminal(stack)) {
                    terminal = stack.copy();
                } else if (UniversalTerminalHelper.isTerminal(stack)) {
                    WirelessTerminalType terminalType = UniversalTerminalHelper.getTerminalType(stack);
                    if (terminalType != null) {
                        terminals.add(terminalType);
                    }
                }
            }
        }

        if (isUniversal) {
            for (WirelessTerminalType terminalType : terminals) {
                UniversalTerminalHelper.installModule(terminal, terminalType);
            }
        } else {
            WirelessTerminalType terminalType = UniversalTerminalHelper.getTerminalType(terminal);
            ItemStack universalTerminal = NAE2.definitions().items().universalWirelessTerminal().maybeStack(1).orElse(ItemStack.EMPTY);

            universalTerminal.setTagCompound(terminal.getTagCompound());

            if (terminalType != null) {
                UniversalTerminalHelper.installModule(universalTerminal, terminalType);
                Platform.openNbtData(universalTerminal).setInteger("type", terminalType.ordinal());
            }

            if (terminalType == WirelessTerminalType.PATTERN) {
                NBTTagCompound compound = Platform.openNbtData(universalTerminal);
                compound.setTag("craftingGridPattern",compound.getTag("craftingGrid"));
                compound.removeTag("craftingGrid");
            }

            terminal = universalTerminal;
            for (WirelessTerminalType x : terminals) {
                UniversalTerminalHelper.installModule(terminal, x);
            }
        }

        return terminal;
    }

    @Override
    public boolean canFit(int width, int height) {
        return (width >= 1 && height >= 2) || (width >= 2 && height >= 1);
    }

    @Override
    public ItemStack getRecipeOutput() {
        if (NAE2.definitions().items().universalWirelessTerminal().maybeStack(1).isPresent()) {
            ItemStack itemStack = NAE2.definitions().items().universalWirelessTerminal().maybeStack(1).get();
            itemStack.setItemDamage(1);

            return itemStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }
}
