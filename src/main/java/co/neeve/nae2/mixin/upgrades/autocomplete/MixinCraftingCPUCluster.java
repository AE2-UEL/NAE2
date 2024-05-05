package co.neeve.nae2.mixin.upgrades.autocomplete;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.MECraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import co.neeve.nae2.common.interfaces.ICancellingCraftingMedium;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinCraftingCPUCluster {
	@Unique
	protected boolean nae2$ghostInjecting;
	@Shadow
	private MachineSource machineSrc;

	@Shadow
	public abstract IAEItemStack injectItems(IAEItemStack input, Actionable type, IActionSource src);

	/**
	 * Set up a list of items we should feed back into the CPU from pushed auto-cancelling patterns.
	 */
	@Inject(method = "executeCrafting", at = @At("HEAD"))
	protected void executeCraftingHead(CallbackInfo ci,
	                                   @Share(value = "nae2void") LocalRef<List<IAEItemStack>> voidSet) {
		voidSet.set(new ArrayList<>());
	}

	/**
	 * Feed the list back into the CPU, marking that the incoming insertion is bogus.
	 */
	@Inject(method = "executeCrafting", at = @At("RETURN"))
	protected void executeCraftingReturn(CallbackInfo ci,
	                                     @Share(value = "nae2void") LocalRef<List<IAEItemStack>> voidSet) {
		for (var output : voidSet.get()) {
			this.nae2$ghostInject(output);
		}
	}

	@Unique
	protected void nae2$ghostInject(IAEItemStack output) {
		this.nae2$ghostInjecting = true;
		try {
			this.injectItems(output, Actionable.MODULATE, this.machineSrc);
		} finally {
			this.nae2$ghostInjecting = false;
		}
	}

	/**
	 * Collect any items marked for auto-completion to reinject them back.
	 */
	@ModifyExpressionValue(
		method = "executeCrafting",
		at = @At(
			value = "INVOKE",
			target = "Lappeng/api/networking/crafting/ICraftingPatternDetails;getCondensedOutputs()" +
				"[Lappeng/api/storage/data/IAEItemStack;"
		)
	)
	protected IAEItemStack[] modifyCondensedOutputs(IAEItemStack[] original, @Local ICraftingMedium medium,
	                                                @Share(value = "nae2void") LocalRef<List<IAEItemStack>> voidSet) {
		if (medium instanceof ICancellingCraftingMedium craftingMedium && craftingMedium.shouldAutoComplete()) {
			for (var output : original) {
				voidSet.get().add(output);
			}
		}

		return original;
	}

	@WrapOperation(method = "injectItems", at = @At(value = "INVOKE", target = "Lappeng/crafting/CraftingLink;" +
		"injectItems(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/Actionable;)" +
		"Lappeng/api/storage/data/IAEItemStack;"))
	protected IAEItemStack wrapInjectItems(CraftingLink link, IAEItemStack item, Actionable actionable,
	                                       Operation<IAEItemStack> operation) {
		// Prevent the items from actually being injected into the CPU. We only the metadata to be updated.
		if (this.nae2$ghostInjecting) {
			return null;
		}

		return operation.call(link, item, actionable);
	}

	@WrapOperation(method = "injectItems", at = @At(value = "INVOKE", target = "Lappeng/crafting" +
		"/MECraftingInventory;" +
		"injectItems(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/Actionable;" +
		"Lappeng/api/networking/security/IActionSource;)Lappeng/api/storage/data/IAEItemStack;"))
	protected IAEItemStack wrapInjectItems(MECraftingInventory link, IAEItemStack item, Actionable actionable,
	                                       IActionSource source,
	                                       Operation<IAEItemStack> operation) {
		// Prevent the items from actually being injected into the CPU. We only the metadata to be updated.
		if (this.nae2$ghostInjecting) {
			return null;
		}

		return operation.call(link, item, actionable, source);
	}
}
