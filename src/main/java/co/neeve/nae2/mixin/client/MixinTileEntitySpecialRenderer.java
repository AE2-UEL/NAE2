package co.neeve.nae2.mixin.client;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntitySpecialRenderer.class)
public abstract class MixinTileEntitySpecialRenderer<T extends TileEntity> {
	@Shadow
	public abstract boolean isGlobalRenderer(T te);
}
