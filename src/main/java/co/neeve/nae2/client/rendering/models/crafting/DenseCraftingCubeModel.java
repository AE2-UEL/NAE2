package co.neeve.nae2.client.rendering.models.crafting;//

import appeng.core.AppEng;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.blocks.BlockDenseCraftingUnit;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public
class DenseCraftingCubeModel implements IModel {
	private static final ResourceLocation RING_CORNER = texture("ring_corner");
	private static final ResourceLocation RING_SIDE_HOR = texture("ring_side_hor");
	private static final ResourceLocation RING_SIDE_VER = texture("ring_side_ver");
	private static final ResourceLocation UNIT_BASE = texture("unit_base");
	private static final ResourceLocation LIGHT_BASE = texture("light_base");
	private static final ResourceLocation MONITOR_BASE = texture("monitor_base");
	private static final ResourceLocation MONITOR_LIGHT_DARK = texture("monitor_light_dark");
	private static final ResourceLocation MONITOR_LIGHT_MEDIUM = texture("monitor_light_medium");
	private static final ResourceLocation MONITOR_LIGHT_BRIGHT = texture("monitor_light_bright");
	private static final ResourceLocation COPROCESSOR_4X_LIGHT = naetexture("coprocessor_4x_light");
	private static final ResourceLocation COPROCESSOR_64X_LIGHT = naetexture("coprocessor_16x_light");
	private static final ResourceLocation COPROCESSOR_16X_LIGHT = naetexture("coprocessor_64x_light");
	private static final ResourceLocation STORAGE_256K_LIGHT = naetexture("crafting_storage_256k_light");
	private static final ResourceLocation STORAGE_1024K_LIGHT = naetexture("crafting_storage_1024k_light");
	private static final ResourceLocation STORAGE_4096K_LIGHT = naetexture("crafting_storage_4096k_light");
	private static final ResourceLocation STORAGE_16384K_LIGHT = naetexture("crafting_storage_16384k_light");
	private final BlockDenseCraftingUnit.DenseCraftingUnitType type;

	public DenseCraftingCubeModel(BlockDenseCraftingUnit.DenseCraftingUnitType type) {
		this.type = type;
	}

	private static TextureAtlasSprite getLightTexture(Function<ResourceLocation, TextureAtlasSprite> textureGetter,
	                                                  BlockDenseCraftingUnit.DenseCraftingUnitType type) {
		return switch (type) {
			case STORAGE_256K -> textureGetter.apply(STORAGE_256K_LIGHT);
			case STORAGE_1024K -> textureGetter.apply(STORAGE_1024K_LIGHT);
			case STORAGE_4096K -> textureGetter.apply(STORAGE_4096K_LIGHT);
			case STORAGE_16384K -> textureGetter.apply(STORAGE_16384K_LIGHT);
			case COPROCESSOR_4X -> textureGetter.apply(COPROCESSOR_4X_LIGHT);
			case COPROCESSOR_16X -> textureGetter.apply(COPROCESSOR_16X_LIGHT);
			case COPROCESSOR_64X -> textureGetter.apply(COPROCESSOR_64X_LIGHT);
		};
	}

	private static ResourceLocation texture(String name) {
		return new ResourceLocation(AppEng.MOD_ID, "blocks/crafting/" + name);
	}

	private static ResourceLocation naetexture(String name) {
		return new ResourceLocation(Tags.MODID, "block/crafting/" + name);
	}

	public @NotNull Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	public @NotNull Collection<ResourceLocation> getTextures() {
		return ImmutableList.of(RING_CORNER,
			RING_SIDE_HOR,
			RING_SIDE_VER,
			UNIT_BASE,
			LIGHT_BASE,
			COPROCESSOR_4X_LIGHT,
			COPROCESSOR_16X_LIGHT,
			COPROCESSOR_64X_LIGHT,
			STORAGE_256K_LIGHT,
			STORAGE_1024K_LIGHT,
			STORAGE_4096K_LIGHT,
			STORAGE_16384K_LIGHT,
			MONITOR_BASE,
			MONITOR_LIGHT_DARK,
			MONITOR_LIGHT_MEDIUM, MONITOR_LIGHT_BRIGHT);
	}

	public @NotNull IBakedModel bake(@NotNull IModelState state, @NotNull VertexFormat format,
	                                 Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		var ringCorner = bakedTextureGetter.apply(RING_CORNER);
		var ringSideHor = bakedTextureGetter.apply(RING_SIDE_HOR);
		var ringSideVer = bakedTextureGetter.apply(RING_SIDE_VER);

		return new DenseLightBakedModel(format,
			ringCorner,
			ringSideHor,
			ringSideVer,
			bakedTextureGetter.apply(LIGHT_BASE),
			getLightTexture(bakedTextureGetter, this.type));

	}

	public @NotNull IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}
}
