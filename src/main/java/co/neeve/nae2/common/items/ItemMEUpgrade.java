package co.neeve.nae2.common.items;

import co.neeve.nae2.common.integration.opencomputer.WirelessHandlerUpgradeAE;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMEUpgrade extends Item {

    public ItemMEUpgrade() {
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 2));
            items.add(new ItemStack(this, 1, 1));
            items.add(new ItemStack(this, 1, 0));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + stack.getItemDamage();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String key = WirelessHandlerUpgradeAE.INSTANCE.getEncryptionKey(stack);
        if (!key.isEmpty()) {
            tooltip.add(I18n.format("nae2.upgrade_me.linked"));
        } else {
            tooltip.add(I18n.format("nae2.upgrade_me.unlinked"));
        }

        switch (stack.getItemDamage()) {
            case 0:
                tooltip.add(I18n.format("nae2.upgrade_me.range_tier3"));
                break;
            case 1:
                tooltip.add(I18n.format("nae2.upgrade_me.range_tier2"));
                break;
            default:
                tooltip.add(I18n.format("nae2.upgrade_me.range_tier1"));
                break;
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return switch (stack.getItemDamage()) {
            case 1 -> EnumRarity.UNCOMMON;
            case 0 -> EnumRarity.RARE;
            default -> EnumRarity.COMMON;
        };
    }
}
