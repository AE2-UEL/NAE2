package co.neeve.nae2.core.common;

import co.neeve.nae2.item.patternmultiplier.ItemPatternMultiplier;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class Items {
    public static ImmutableList<Item> ITEMS;
    public static void init() {
        Items.ITEMS = ImmutableList.<Item>builder().add(new ItemPatternMultiplier()).build();
    }

    @SubscribeEvent
    public void register(final RegistryEvent.Register<Item> event)
    {
        ITEMS.forEach(item -> {
            event.getRegistry().register(item);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        });
    }
}
