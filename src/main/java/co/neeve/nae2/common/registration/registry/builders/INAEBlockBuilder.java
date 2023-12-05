//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package co.neeve.nae2.common.registration.registry.builders;

import appeng.api.definitions.IBlockDefinition;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import co.neeve.nae2.common.features.IFeature;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface INAEBlockBuilder {
	INAEBlockBuilder bootstrap(BiFunction<Block, Item, IBootstrapComponent> var1);

	INAEBlockBuilder features(IFeature... var1);

	INAEBlockBuilder rendering(BlockRenderingCustomizer var1);

	INAEBlockBuilder tileEntity(TileEntityDefinition var1);

	INAEBlockBuilder disableItem();

	INAEBlockBuilder useCustomItemModel();

	INAEBlockBuilder item(Function<Block, ItemBlock> var1);

	<T extends IBlockDefinition> T build();

	INAEBlockBuilder withJEIDescription();
}
