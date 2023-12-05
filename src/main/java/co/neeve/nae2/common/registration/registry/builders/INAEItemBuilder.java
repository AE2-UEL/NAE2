//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package co.neeve.nae2.common.registration.registry.builders;

import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.core.features.ItemDefinition;
import co.neeve.nae2.common.features.IFeature;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

public interface INAEItemBuilder {
	INAEItemBuilder bootstrap(Function<Item, IBootstrapComponent> var1);

	INAEItemBuilder features(IFeature... var1);


	INAEItemBuilder creativeTab(CreativeTabs var1);

	INAEItemBuilder rendering(ItemRenderingCustomizer var1);

	INAEItemBuilder dispenserBehavior(Supplier<IBehaviorDispenseItem> var1);

	ItemDefinition build();

	INAEItemBuilder hide();
}
