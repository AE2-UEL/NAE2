package co.neeve.nae2.common.registration.registry.rendering;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NoItemRendering extends ItemRenderingCustomizer {
	public static final ModelResourceLocation MODEL_MISSING = new ModelResourceLocation("builtin/missing", "missing");

	public NoItemRendering() {}

	@Override
	@SideOnly(Side.CLIENT)
	public void customize(IItemRendering rendering) {
		rendering.meshDefinition(is -> MODEL_MISSING);
	}
}
