package co.neeve.nae2.common.registration.registry.rendering;

import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.StaticItemColor;
import co.neeve.nae2.common.items.NAEBaseItemPart;
import co.neeve.nae2.common.registration.definitions.Parts;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ItemPartRendering extends ItemRenderingCustomizer {

	private final NAEBaseItemPart item;

	public ItemPartRendering(NAEBaseItemPart item) {
		this.item = item;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void customize(IItemRendering rendering) {

		rendering.meshDefinition(this::getItemMeshDefinition);

		rendering.color(new StaticItemColor(AEColor.TRANSPARENT));

		// Register all item models as variants so they get loaded
		rendering.variants(Arrays.stream(Parts.PartType.values())
			.flatMap(part -> part.getItemModels().stream())
			.collect(Collectors.toList()));
	}

	private ModelResourceLocation getItemMeshDefinition(ItemStack is) {
		var partType = this.item.getTypeByStack(is);
		var variant = this.item.variantOf(is.getItemDamage());
		return partType.getItemModels().get(variant);
	}
}