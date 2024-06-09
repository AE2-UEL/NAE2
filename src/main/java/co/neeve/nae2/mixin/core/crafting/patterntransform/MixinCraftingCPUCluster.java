package co.neeve.nae2.mixin.core.crafting.patterntransform;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.common.crafting.patterntransform.PatternTransformWrapper;
import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Map;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinCraftingCPUCluster {
	@WrapOperation(
		method = "executeCrafting", at = @At(
		value = "INVOKE",
		target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern" +
			"(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"
	))
	private boolean unwrapPatternWrapper(ICraftingMedium self, ICraftingPatternDetails details,
	                                     InventoryCrafting inventory,
	                                     Operation<Boolean> operation) {
		final ICraftingPatternDetails newDetails;
		if (details instanceof PatternTransformWrapper wrapper) {
			newDetails = wrapper.getDelegate();
		} else {
			newDetails = details;
		}

		return operation.call(self, newDetails, inventory);
	}

	@Inject(
		method = "writeToNBT",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/NBTTagList;appendTag(Lnet/minecraft/nbt/NBTBase;)V",
			remap = true
		)
	)
	private void writeTransforms(CallbackInfo info,
	                             @Local(name = "item") NBTTagCompound item,
	                             @Local Map.Entry<ICraftingPatternDetails, Object> e) {
		if (e.getKey() instanceof PatternTransformWrapper wrapper) {
			var transforms = new NBTTagCompound();
			var toWrite = ImmutableMap.of(
				"inputs", wrapper.getInputs(),
				"outputs", wrapper.getOutputs()
			);

			for (var entry : toWrite.entrySet()) {
				var list = new NBTTagList();
				for (var iaeItemStack : entry.getValue()) {
					var aeItemStackNBT = new NBTTagCompound();
					iaeItemStack.writeToNBT(aeItemStackNBT);
					list.appendTag(aeItemStackNBT);
				}

				transforms.setTag(entry.getKey(), list);
			}

			item.setTag("nae2$transforms", transforms);
		}
	}

	@ModifyExpressionValue(method = "readFromNBT",
		at = @At(
			value = "INVOKE",
			target = "Lappeng/api/implementations/ICraftingPatternItem;getPatternForItem" +
				"(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;)" +
				"Lappeng/api/networking/crafting/ICraftingPatternDetails;"
		)
	)
	private ICraftingPatternDetails readFromNBT(ICraftingPatternDetails details,
	                                            @Local(name = "item") NBTTagCompound item) {
		if (item.hasKey("nae2$transforms")) {
			var transforms = item.getCompoundTag("nae2$transforms");
			var toRead = ImmutableMap.of(
				"inputs", transforms.getTagList("inputs", 10),
				"outputs", transforms.getTagList("outputs", 10)
			);

			var read = ImmutableMap.of(
				"inputs", new ArrayList<IAEItemStack>(),
				"outputs", new ArrayList<IAEItemStack>()
			);

			for (var entry : toRead.entrySet()) {
				var list = entry.getValue();
				for (var serializedAEStack : list) {
					var aeItemStack = AEItemStack.fromNBT((NBTTagCompound) serializedAEStack);
					if (aeItemStack != null) {
						read.get(entry.getKey()).add(aeItemStack);
					}
				}
			}

			return new PatternTransformWrapper(details,
				read.get("inputs").stream().toArray(IAEItemStack[]::new),
				read.get("outputs").stream().toArray(IAEItemStack[]::new));
		}

		return details;
	}
}
