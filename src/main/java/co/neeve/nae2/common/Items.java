package co.neeve.nae2.common;

import appeng.api.config.Upgrades;
import co.neeve.nae2.Tags;
import co.neeve.nae2.items.patternmultitool.ToolPatternMultiTool;
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
	public static ToolPatternMultiTool PATTERN_MULTI_TOOL;
	public static ImmutableList<Item> ITEMS;

	public void init() {
		Upgrades.CAPACITY.registerItem(new ItemStack(PATTERN_MULTI_TOOL), 3);
	}

	@SubscribeEvent
	public void register(final RegistryEvent.Register<Item> event) {
		Items.ITEMS = ImmutableList.<Item>builder().add(PATTERN_MULTI_TOOL = new ToolPatternMultiTool()).build();

		ITEMS.forEach(item -> {
			event.getRegistry().register(item);
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				ModelLoader.setCustomModelResourceLocation(item, 0,
					new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
			}
		});
	}

	@SubscribeEvent
	public void remap(final RegistryEvent.MissingMappings<Item> event) {
		event.getMappings().forEach(x -> {
			if (x.key.getNamespace().equals(Tags.MODID) && x.key.getPath().equals("pattern_multiplier")) {
				x.remap(PATTERN_MULTI_TOOL);
			}
		});
	}
}
