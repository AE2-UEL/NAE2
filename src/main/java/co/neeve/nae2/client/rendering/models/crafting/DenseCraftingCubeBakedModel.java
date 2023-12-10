package co.neeve.nae2.client.rendering.models.crafting;

import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.cablebus.CubeBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@SideOnly(Side.CLIENT)
abstract class DenseCraftingCubeBakedModel implements IBakedModel {
	private final VertexFormat format;
	private final TextureAtlasSprite ringCorner;
	private final TextureAtlasSprite ringHor;
	private final TextureAtlasSprite ringVer;

	DenseCraftingCubeBakedModel(VertexFormat format, TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor,
	                            TextureAtlasSprite ringVer) {
		this.format = format;
		this.ringCorner = ringCorner;
		this.ringHor = ringHor;
		this.ringVer = ringVer;
	}

	private static EnumSet<EnumFacing> getConnections(@Nullable IBlockState state) {
		if (!(state instanceof IExtendedBlockState extState)) {
			return EnumSet.noneOf(EnumFacing.class);
		} else {
			var cubeState = extState.getValue(BlockCraftingUnit.STATE);
			return cubeState == null ? EnumSet.noneOf(EnumFacing.class) : cubeState.getConnections();
		}
	}

	public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (side == null) {
			return Collections.emptyList();
		} else {
			var connections = getConnections(state);
			List<BakedQuad> quads = new ArrayList<>();
			var builder = new CubeBuilder(this.format, quads);
			builder.setDrawFaces(EnumSet.of(side));
			this.addRing(builder, side, connections);
			var x2 = connections.contains(EnumFacing.EAST) ? 16.0F : 13.01F;
			var x1 = connections.contains(EnumFacing.WEST) ? 0.0F : 2.99F;
			var y2 = connections.contains(EnumFacing.UP) ? 16.0F : 13.01F;
			var y1 = connections.contains(EnumFacing.DOWN) ? 0.0F : 2.99F;
			var z2 = connections.contains(EnumFacing.SOUTH) ? 16.0F : 13.01F;
			var z1 = connections.contains(EnumFacing.NORTH) ? 0.0F : 2.99F;
			switch (side) {
				case DOWN, UP -> {
					y1 = 0.0F;
					y2 = 16.0F;
				}
				case NORTH, SOUTH -> {
					z1 = 0.0F;
					z2 = 16.0F;
				}
				case WEST, EAST -> {
					x1 = 0.0F;
					x2 = 16.0F;
				}
			}

			this.addInnerCube(state, builder, x1, y1, z1, x2, y2, z2);
			return quads;
		}
	}

	private void addRing(CubeBuilder builder, EnumFacing side, EnumSet<EnumFacing> connections) {
		builder.setTexture(this.ringCorner);
		this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH);
		this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.EAST, EnumFacing.SOUTH);
		this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH);
		this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.WEST, EnumFacing.SOUTH);
		this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.NORTH);
		this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.SOUTH);
		this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH);
		this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.SOUTH);
		var var4 = EnumFacing.values();

		for (var a : var4) {
			if (a != side && a != side.getOpposite()) {
				if (side.getAxis() != EnumFacing.Axis.Y && (a == EnumFacing.NORTH || a == EnumFacing.EAST || a == EnumFacing.WEST || a == EnumFacing.SOUTH)) {
					builder.setTexture(this.ringVer);
				} else if (side.getAxis() == EnumFacing.Axis.Y && (a == EnumFacing.EAST || a == EnumFacing.WEST)) {
					builder.setTexture(this.ringVer);
				} else {
					builder.setTexture(this.ringHor);
				}

				if (!connections.contains(a)) {
					var x1 = 0.0F;
					var y1 = 0.0F;
					var z1 = 0.0F;
					var x2 = 16.0F;
					var y2 = 16.0F;
					var z2 = 16.0F;
					switch (a) {
						case DOWN -> {
							y1 = 0.0F;
							y2 = 3.0F;
						}
						case UP -> y1 = 13.0F;
						case NORTH -> {
							z1 = 0.0F;
							z2 = 3.0F;
						}
						case SOUTH -> z1 = 13.0F;
						case WEST -> {
							x1 = 0.0F;
							x2 = 3.0F;
						}
						case EAST -> x1 = 13.0F;
					}

					var perpendicular = a.rotateAround(side.getAxis());

					for (var cornerCandidate : EnumSet.of(perpendicular, perpendicular.getOpposite())) {
						if (!connections.contains(cornerCandidate)) {
							switch (cornerCandidate) {
								case DOWN -> y1 = 3.0F;
								case UP -> y2 = 13.0F;
								case NORTH -> z1 = 3.0F;
								case SOUTH -> z2 = 13.0F;
								case WEST -> x1 = 3.0F;
								case EAST -> x2 = 13.0F;
							}
						}
					}

					builder.addCube(x1, y1, z1, x2, y2, z2);
				}
			}
		}

	}

	private void addCornerCap(CubeBuilder builder, EnumSet<EnumFacing> connections, EnumFacing side, EnumFacing down,
	                          EnumFacing west, EnumFacing north) {
		if (!connections.contains(down) && !connections.contains(west) && !connections.contains(north)) {
			if (side == down || side == west || side == north) {
				var x1 = (float) (west == EnumFacing.WEST ? 0 : 13);
				var y1 = (float) (down == EnumFacing.DOWN ? 0 : 13);
				var z1 = (float) (north == EnumFacing.NORTH ? 0 : 13);
				var x2 = (float) (west == EnumFacing.WEST ? 3 : 16);
				var y2 = (float) (down == EnumFacing.DOWN ? 3 : 16);
				var z2 = (float) (north == EnumFacing.NORTH ? 3 : 16);
				builder.addCube(x1, y1, z1, x2, y2, z2);
			}
		}
	}

	protected abstract void addInnerCube(IBlockState var2, CubeBuilder var3, float var4, float var5,
	                                     float var6, float var7, float var8, float var9);

	public boolean isAmbientOcclusion() {
		return false;
	}

	public boolean isGui3d() {
		return false;
	}

	public boolean isBuiltInRenderer() {
		return false;
	}

	public @NotNull TextureAtlasSprite getParticleTexture() {
		return this.ringCorner;
	}

	public @NotNull ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}
}
