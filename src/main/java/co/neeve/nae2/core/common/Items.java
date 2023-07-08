package co.neeve.nae2.core.common;

import appeng.api.config.Upgrades;
import co.neeve.nae2.item.patternmultiplier.ItemPatternMultiplier;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

public class Items {
    public static ItemPatternMultiplier PATTERN_MULTIPLIER;
    public static ImmutableList<Item> ITEMS;

    public void init() {
        Upgrades.CAPACITY.registerItem(new ItemStack(PATTERN_MULTIPLIER), 3);
    }

    @SubscribeEvent
    public void register(final RegistryEvent.Register<Item> event) {
        Items.ITEMS = ImmutableList.<Item>builder().add(PATTERN_MULTIPLIER = new ItemPatternMultiplier()).build();

        ITEMS.forEach(item -> {
            event.getRegistry().register(item);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                        new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
            }
        });
    }


}
