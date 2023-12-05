package co.neeve.nae2.mixin.beamformer.client;

import appeng.api.parts.IPartBakedModel;
import appeng.api.parts.IPartModel;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.QuadRotator;
import co.neeve.nae2.common.parts.implementations.PartBeamFormer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static co.neeve.nae2.common.parts.implementations.PartBeamFormer.PRISM_LOC;

@Mixin(value = CableBusBakedModel.class)
public class MixinCableBusBakedModel {
	@Shadow(remap = false)
	@Final
	private Map<ResourceLocation, IBakedModel> partModels;

	@WrapOperation(method = "getQuads", at = @At(
		value = "INVOKE",
		target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;",
		ordinal = 0
	))
	private Object injectBeamFormerSkip(EnumMap<EnumFacing, IPartModel> instance, Object facing,
	                                    Operation<IPartModel> operation) {
		var model = operation.call(instance, facing);
		if (model == PartBeamFormer.MODEL_ON || model == PartBeamFormer.MODEL_OFF) return null;
		return model;
	}

	@Inject(method = "getQuads", at = @At(
		value = "INVOKE",
		target = "Lappeng/client/render/cablebus/FacadeBuilder;buildFacadeQuads" +
			"(Lnet/minecraft/util/BlockRenderLayer;" +
			"Lappeng/client/render/cablebus/CableBusRenderState;JLjava/util/List;Ljava/util/function/Function;)V",
		remap = false
	))
	private void injectBeamFormerPrisms(IBlockState state, EnumFacing side, long rand,
	                                    CallbackInfoReturnable<List<BakedQuad>> cir,
	                                    @Local BlockRenderLayer layer,
	                                    @Local CableBusRenderState renderState,
	                                    @Local(name = "quads") List<BakedQuad> quads) {
		// yeah, we'll have to do that again.
		if (layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT) {
			for (var facing : EnumFacing.values()) {
				var partModel = renderState.getAttachments().get(facing);

				// Take over to render the prism correctly. If it's beaming, then we won't even get here.
				// Carbon copy of AE2, mostly.
				if (partModel == PartBeamFormer.MODEL_ON || partModel == PartBeamFormer.MODEL_OFF) {
					for (var model : partModel.getModels()) {
						if (model != PRISM_LOC && layer == BlockRenderLayer.TRANSLUCENT) continue;
						if (model == PRISM_LOC && layer != BlockRenderLayer.TRANSLUCENT) continue;
						var bakedModel = this.partModels.get(model);

						List<BakedQuad> partQuads;
						if (bakedModel instanceof IPartBakedModel) {
							partQuads =
								((IPartBakedModel) bakedModel).getPartQuads(renderState.getPartFlags().get(facing),
									rand);
						} else {
							partQuads = bakedModel.getQuads(state, null, rand);
						}

						var rotator = new QuadRotator();
						partQuads = rotator.rotateQuads(partQuads, facing, EnumFacing.UP);
						quads.addAll(partQuads);
					}
				}
			}
		}
	}
}
