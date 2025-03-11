package co.neeve.nae2.common.helpers;

import appeng.api.AEApi;
import appeng.util.Platform;
import co.neeve.nae2.common.api.config.WirelessTerminalType;
import com.glodblock.github.loader.FCItems;
import com.mekeng.github.common.ItemAndBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class UniversalTerminalHelper {
    private final static boolean isMekELoaded = Platform.isModLoaded("mekeng");
    public static final List<ItemStack> wirelessTerminals = new ArrayList<>();
    public static final List<ItemStack> terminals = new ArrayList<>();
    public static final List<WirelessTerminalType> WIRELESS_TERMINAL_TYPE_LIST = Arrays.asList(WirelessTerminalType.values());

    private static final boolean isMekEngLoaded = Platform.isModLoaded("mekeng");
    private static final boolean isAE2FCLoaded = Platform.isModLoaded("ae2fc");

    static {
        wirelessTerminals.add(AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).orElse(null));
        wirelessTerminals.add(AEApi.instance().definitions().items().wirelessFluidTerminal().maybeStack(1).orElse(null));
        wirelessTerminals.add(AEApi.instance().definitions().items().wirelessCraftingTerminal().maybeStack(1).orElse(null));
        wirelessTerminals.add(AEApi.instance().definitions().items().wirelessPatternTerminal().maybeStack(1).orElse(null));

        terminals.add(AEApi.instance().definitions().parts().terminal().maybeStack(1).orElse(null));
        terminals.add(AEApi.instance().definitions().parts().craftingTerminal().maybeStack(1).orElse(null));
        terminals.add(AEApi.instance().definitions().parts().patternTerminal().maybeStack(1).orElse(null));
        terminals.add(AEApi.instance().definitions().parts().fluidTerminal().maybeStack(1).orElse(null));

        if (isMekEngLoaded) {
            wirelessTerminals.add(new ItemStack(ItemAndBlocks.WIRELESS_GAS_TERMINAL));
            terminals.add(new ItemStack(ItemAndBlocks.GAS_TERMINAL));
        }

        if (isAE2FCLoaded) {
            wirelessTerminals.add(new ItemStack(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL));
            terminals.add(new ItemStack(FCItems.PART_FLUID_PATTERN_TERMINAL));
        }
    }

    public static boolean isWirelessTerminal(ItemStack stack) {
        if (stack == ItemStack.EMPTY) return false;

        int itemDamage = stack.getItemDamage();
        Item item = stack.getItem();

        ItemStack wirelessTerminal = AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).orElse(null);
        if (wirelessTerminal != null && wirelessTerminal.getItem() == item && wirelessTerminal.getItemDamage() == itemDamage)
            return true;

        ItemStack wirelessFluidTerminal = AEApi.instance().definitions().items().wirelessFluidTerminal().maybeStack(1).orElse(null);
        if (wirelessFluidTerminal != null && wirelessFluidTerminal.getItem() == item && wirelessFluidTerminal.getItemDamage() == itemDamage)
            return true;

        if (isMekEngLoaded) {
            ItemStack wirelessGasTerminal = new ItemStack(ItemAndBlocks.WIRELESS_GAS_TERMINAL);
            if (wirelessGasTerminal.getItem() == item && wirelessGasTerminal.getItemDamage() == itemDamage) return true;
        }

        if (isAE2FCLoaded) {
            ItemStack fluidPatternWirelessTerminal = new ItemStack(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL);
            if (fluidPatternWirelessTerminal.getItem() == item && fluidPatternWirelessTerminal.getItemDamage() == itemDamage) return true;
        }

        ItemStack wirelessCraftingTerminal = AEApi.instance().definitions().items().wirelessCraftingTerminal().maybeStack(1).orElse(null);
        if (wirelessCraftingTerminal != null && wirelessCraftingTerminal.getItem() == item && wirelessCraftingTerminal.getItemDamage() == itemDamage)
            return true;

        ItemStack wirelessPatternTerminal = AEApi.instance().definitions().items().wirelessPatternTerminal().maybeStack(1).orElse(null);
        if (wirelessPatternTerminal != null && wirelessPatternTerminal.getItem() == item && wirelessPatternTerminal.getItemDamage() == itemDamage)
            return true;

        return false;
    }

    public static boolean isTerminal(ItemStack stack) {
        if (stack == ItemStack.EMPTY) return false;

        Item item = stack.getItem();
        int itemDamage = stack.getItemDamage();

        ItemStack terminal = AEApi.instance().definitions().parts().terminal().maybeStack(1).orElse(null);
        if (terminal != null && terminal.getItem() == item && terminal.getItemDamage() == itemDamage) return true;

        ItemStack fluidTerminal = AEApi.instance().definitions().parts().fluidTerminal().maybeStack(1).orElse(null);
        if (fluidTerminal != null && fluidTerminal.getItem() == item && fluidTerminal.getItemDamage() == itemDamage)
            return true;

        if (isMekEngLoaded) {
            ItemStack gasTerminal = new ItemStack(ItemAndBlocks.GAS_TERMINAL);
            if (gasTerminal.getItem() == item && gasTerminal.getItemDamage() == itemDamage) return true;
        }

        if (isAE2FCLoaded) {
            ItemStack fluidPatternTerminal = new ItemStack(FCItems.PART_FLUID_PATTERN_TERMINAL);
            if (fluidPatternTerminal.getItem() == item && fluidPatternTerminal.getItemDamage() == itemDamage) return true;
        }

        ItemStack craftingTerminal = AEApi.instance().definitions().parts().craftingTerminal().maybeStack(1).orElse(null);
        if (craftingTerminal != null && craftingTerminal.getItem() == item && craftingTerminal.getItemDamage() == itemDamage)
            return true;

        ItemStack patternTerminal = AEApi.instance().definitions().parts().patternTerminal().maybeStack(1).orElse(null);
        if (patternTerminal != null && patternTerminal.getItem() == item && patternTerminal.getItemDamage() == itemDamage)
            return true;

        ItemStack interfaceTerminal = AEApi.instance().definitions().parts().interfaceTerminal().maybeStack(1).orElse(null);
        if (interfaceTerminal != null && interfaceTerminal.getItem() == item && interfaceTerminal.getItemDamage() == itemDamage)
            return true;

        return false;
    }

    public static WirelessTerminalType getTerminalType(ItemStack stack) {
        if (stack == ItemStack.EMPTY) return null;

        Item item = stack.getItem();
        int itemDamage = stack.getItemDamage();

        ItemStack terminal = AEApi.instance().definitions().parts().terminal().maybeStack(1).orElse(null);
        if (terminal != null && terminal.getItem() == item && terminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.ITEM;
        }

        ItemStack fluidTerminal = AEApi.instance().definitions().parts().fluidTerminal().maybeStack(1).orElse(null);
        if (fluidTerminal != null && fluidTerminal.getItem() == item && fluidTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.FLUID;
        }

        ItemStack craftingTerminal = AEApi.instance().definitions().parts().craftingTerminal().maybeStack(1).orElse(null);
        if (craftingTerminal != null && fluidTerminal.getItem() == item && craftingTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.CRAFTING;
        }

        ItemStack patternTerminal = AEApi.instance().definitions().parts().patternTerminal().maybeStack(1).orElse(null);
        if (patternTerminal != null && patternTerminal.getItem() == item && patternTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.PATTERN;
        }

        ItemStack interfaceTerminal = AEApi.instance().definitions().parts().interfaceTerminal().maybeStack(1).orElse(null);
        if (interfaceTerminal != null && interfaceTerminal.getItem() == item && interfaceTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.INTERFACE;
        }

        //Wireless Terminal

        ItemStack wirelessTerminal = AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).orElse(null);
        if (wirelessTerminal != null && wirelessTerminal.getItem() == item && wirelessTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.ITEM;
        }

        ItemStack wirelessFluidTerminal = AEApi.instance().definitions().items().wirelessFluidTerminal().maybeStack(1).orElse(null);
        if (wirelessFluidTerminal != null && wirelessFluidTerminal.getItem() == item && wirelessFluidTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.FLUID;
        }

        ItemStack wirelessCraftingTerminal = AEApi.instance().definitions().items().wirelessCraftingTerminal().maybeStack(1).orElse(null);
        if (wirelessCraftingTerminal != null && wirelessCraftingTerminal.getItem() == item && wirelessCraftingTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.CRAFTING;
        }

        ItemStack wirelessPatternTerminal = AEApi.instance().definitions().items().wirelessPatternTerminal().maybeStack(1).orElse(null);
        if (wirelessPatternTerminal != null && wirelessPatternTerminal.getItem() == item && wirelessPatternTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.PATTERN;
        }

        ItemStack wirelessInterfaceTerminal = AEApi.instance().definitions().items().wirelessInterfaceTerminal().maybeStack(1).orElse(null);
        if (wirelessInterfaceTerminal != null && wirelessInterfaceTerminal.getItem() == item && wirelessInterfaceTerminal.getItemDamage() == itemDamage) {
            return WirelessTerminalType.INTERFACE;
        }

        //MekEng Integration
        if (isMekEngLoaded) {
            ItemStack gasTerminal = new ItemStack(ItemAndBlocks.GAS_TERMINAL);
            if (gasTerminal.getItem() == item && gasTerminal.getItemDamage() == itemDamage) {
                return WirelessTerminalType.GAS;
            }

            ItemStack wirelessGasTerminal = new ItemStack(ItemAndBlocks.WIRELESS_GAS_TERMINAL);
            if (wirelessGasTerminal.getItem() == item && wirelessGasTerminal.getItemDamage() == itemDamage) {
                return WirelessTerminalType.GAS;
            }
        }

        if (isAE2FCLoaded) {
            ItemStack fluidPatternTerminal = new ItemStack(FCItems.PART_FLUID_PATTERN_TERMINAL);
            if (fluidPatternTerminal.getItem() == item && fluidPatternTerminal.getItemDamage() == itemDamage) {
                return WirelessTerminalType.FLUID_PATTERN;
            }

            ItemStack fluidPatternWirelessTerminal = new ItemStack(FCItems.WIRELESS_FLUID_PATTERN_TERMINAL);
            if (fluidPatternWirelessTerminal.getItem() == item && fluidPatternWirelessTerminal.getItemDamage() == itemDamage) {
                return WirelessTerminalType.FLUID_PATTERN;
            }
        }

        return null;
    }

    public static boolean isModuleValid(WirelessTerminalType type) {
        return switch (type) {
            case FLUID_PATTERN -> Platform.isModLoaded("ae2fc");
            case GAS -> Platform.isModLoaded("mekeng");
            default -> true;
        };
    }


    public static ItemStack changeMode(ItemStack itemStack, EntityPlayer player, NBTTagCompound tag) {
        EnumSet<WirelessTerminalType> installedModules = getInstalledModules(itemStack);
        int type = tag.getInteger("type");
        WirelessTerminalType terminalType;
        do {
            type = (type + 1) % WIRELESS_TERMINAL_TYPE_LIST.size();
            terminalType = WIRELESS_TERMINAL_TYPE_LIST.get(type);

        } while (!isModuleValid(terminalType) || !installedModules.contains(terminalType));

        tag.setInteger("type", type);

        return itemStack;
    }

    public static EnumSet<WirelessTerminalType> getInstalledModules(ItemStack itemStack) {
        if (itemStack == ItemStack.EMPTY || itemStack.getItem() == ItemStack.EMPTY.getItem()) {
            return EnumSet.noneOf(WirelessTerminalType.class);
        }

        NBTTagCompound tag = Platform.openNbtData(itemStack);
        int installed = tag.hasKey("modules") ? tag.getInteger("modules") : 0;

        EnumSet<WirelessTerminalType> set = EnumSet.noneOf(WirelessTerminalType.class);

        for (WirelessTerminalType x : WirelessTerminalType.values()) {
            if ((installed >> x.ordinal()) % 2 == 1) {
                set.add(x);
            }
        }

        return set;
    }

    public static boolean isInstalled(ItemStack itemStack, WirelessTerminalType module) {
        if (itemStack == ItemStack.EMPTY || itemStack.getItem() == ItemStack.EMPTY.getItem()) {
            return false;
        }

        NBTTagCompound tag = Platform.openNbtData(itemStack);
        int installed = tag.hasKey("modules") ? tag.getInteger("modules") : 0;

        return 1 == (installed >>> module.ordinal()) % 2;
    }

    public static void installModule(ItemStack itemStack, WirelessTerminalType module) {
        if (isInstalled(itemStack, module) || itemStack == ItemStack.EMPTY) {
            return;
        }

        int install = 1 << module.ordinal();

        NBTTagCompound tag = Platform.openNbtData(itemStack);

        int installed;
        if (tag.hasKey("modules")) {
            installed = tag.getInteger("modules") + install;
        } else {
            installed = install;
        }

        tag.setInteger("modules", installed);
    }
}
