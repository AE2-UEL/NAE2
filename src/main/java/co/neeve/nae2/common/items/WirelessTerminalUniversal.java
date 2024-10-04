package co.neeve.nae2.common.items;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.GuiWrapper;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.api.config.WirelessTerminalType;
import com.mekeng.github.common.container.handler.AEGuiBridge;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static co.neeve.nae2.common.helpers.UniversalTerminalHelper.*;

public class WirelessTerminalUniversal extends ToolWirelessTerminal {

    @Override
    public IGuiHandler getGuiHandler(ItemStack is) {
        return switch (Platform.openNbtData(is).getInteger("type")) {
            case 0 -> GuiBridge.GUI_WIRELESS_TERM;
            case 1 -> GuiBridge.GUI_WIRELESS_FLUID_TERMINAL;
            case 2 -> GuiWrapper.INSTANCE.wrap(AEGuiBridge.WIRELESS_GAS_TERM);
            case 3 -> GuiBridge.GUI_WIRELESS_CRAFTING_TERMINAL;
            case 4 -> GuiBridge.GUI_WIRELESS_PATTERN_TERMINAL;
            default -> GuiBridge.GUI_WIRELESS_TERM;
        };
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is.getItem() == NAE2.definitions().items().universalWirelessTerminal().maybeItem().orElse(null);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound tagCompound = Platform.openNbtData(stack);
        if (w.isRemote) {
            if (player.isSneaking()) {
                return new ActionResult(EnumActionResult.SUCCESS, stack);
            }
            if (!tagCompound.hasKey("type")) {
                tagCompound.setInteger("type", 0);
            }
            return new ActionResult(EnumActionResult.SUCCESS, stack);
        }

        if (!tagCompound.hasKey("type")) {
            tagCompound.setInteger("type", 0);
        }
        if (player.isSneaking()) {
            if (stack == ItemStack.EMPTY) {
                return new ActionResult(EnumActionResult.FAIL, stack);
            }
            return new ActionResult(EnumActionResult.SUCCESS, changeMode(stack, player, tagCompound));
        }
        AEApi.instance().registries().wireless().openWirelessTerminalGui(stack, w, player);

        return new ActionResult(EnumActionResult.SUCCESS, stack);
    }



    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound tag = Platform.openNbtData(stack);
        if (!tag.hasKey("type")) {
            tag.setInteger("type", 0);
        }

        return super.getItemStackDisplayName(stack) + " - " + I18n.translateToLocal(
                "nae2.tooltip.universal_wireless_terminal." + WirelessTerminalType.values()[tag.getInteger(
                        "type"
                )].toString().toLowerCase()
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag advancedTooltips) {
        NBTTagCompound tag = Platform.openNbtData(stack);
        if (!tag.hasKey("type")) {
            tag.setInteger("type", 0);
        }

        lines.add(
                I18n.translateToLocal("nae2.tooltip.universal_wireless_terminal.mode") + ": " + I18n.translateToLocal(
                        "nae2.tooltip.universal_wireless_terminal." + WirelessTerminalType.values()[tag.getInteger(
                                "type"
                        )].toString().toLowerCase()
                )
        );

        lines.add(I18n.translateToLocal("nae2.tooltip.universal_wireless_terminal.installed"));
        for (WirelessTerminalType wirelessTerminalType : getInstalledModules(stack)) {
            lines.add("- " + I18n.translateToLocal("nae2.tooltip.universal_wireless_terminal." + wirelessTerminalType.name().toLowerCase()));
        }

        super.addCheckedInformation(stack, world, lines, advancedTooltips);
    }

    @Override
    protected void getCheckedSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> itemStacks) {
        if (!isInCreativeTab(creativeTab)) {
            return;
        }
        int modulesValue = 0;
        for (WirelessTerminalType type : WirelessTerminalType.values()) {
            modulesValue |= (1 << type.ordinal());
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("modules", modulesValue);
        ItemStack itemStack = new ItemStack(this);
        itemStack.setTagCompound(tag);
        itemStacks.add(itemStack.copy());
        injectAEPower(itemStack, getAEMaxPower(itemStack), Actionable.MODULATE);
        itemStacks.add(itemStack);
    }


}
