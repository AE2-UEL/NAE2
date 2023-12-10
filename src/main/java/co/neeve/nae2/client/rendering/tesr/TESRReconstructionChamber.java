package co.neeve.nae2.client.rendering.tesr;

import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class TESRReconstructionChamber extends TileEntitySpecialRenderer<TileReconstructionChamber> {
	private static void renderIS(@NotNull TileReconstructionChamber te, double x, double y, double z,
	                             float partialTicks, ItemStack is, float scale, int color) {
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();

		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.scale(scale, scale, scale);

		var angle = (te.getWorld().getTotalWorldTime() + partialTicks) * 4;
		GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);

		GlStateManager.translate(-0.5f, -0.5f, -0.5f);

		var ri = Minecraft.getMinecraft().getRenderItem();
		var ibakedmodel = ri.getItemModelWithOverrides(is, null, null);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ZERO);

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		ri.renderModel(ibakedmodel, color);
		GlStateManager.disableBlend();

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	public void render(@NotNull TileReconstructionChamber te, double x, double y, double z, float partialTicks,
	                   int destroyStage, float alpha) {
		var is = te.getDisplayStack();
		if (!is.isEmpty()) {
			var scale = 0.5f;
			renderIS(te, x, y, z, partialTicks, is, scale, 0xFFFFFFFF);
		}

		var currentTime = te.getWorld().getTotalWorldTime();

		var iter = te.getHolograms().iterator();
		while (iter.hasNext()) {
			var holo = iter.next();
			is = holo.getHoloStack();
			var progress = holo.getProgress(currentTime + partialTicks);
			if (progress >= 1) {
				iter.remove();
			} else {
				var f = this.easeOutCirc(progress);

				renderIS(te, x, y, z, partialTicks, is, (float) f * 0.25f + 0.5f,
					0x00FFFFFF | (int) Math.floor((1f - f) * 255) << 24);
			}
		}
	}

	double easeOutCirc(double x) {
		return Math.sqrt(1 - Math.pow(x - 1, 2));
	}
}
