package co.neeve.nae2.client.rendering.models.crafting;

import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.cablebus.CubeBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class DenseLightBakedModel extends DenseCraftingCubeBakedModel {
	private final TextureAtlasSprite baseTexture;
	private final TextureAtlasSprite lightTexture;

	DenseLightBakedModel(VertexFormat format, TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor,
	                     TextureAtlasSprite ringVer, TextureAtlasSprite baseTexture, TextureAtlasSprite lightTexture) {
		super(format, ringCorner, ringHor, ringVer);
		this.baseTexture = baseTexture;
		this.lightTexture = lightTexture;
	}

	protected void addInnerCube(IBlockState state, CubeBuilder builder, float x1, float y1,
	                            float z1, float x2, float y2, float z2) {
		builder.setTexture(this.baseTexture);
		builder.addCube(x1, y1, z1, x2, y2, z2);
		boolean powered = state.getValue(BlockCraftingUnit.POWERED);
		builder.setRenderFullBright(powered);
		builder.setTexture(this.lightTexture);
		builder.addCube(x1, y1, z1, x2, y2, z2);
	}
}
