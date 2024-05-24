package co.neeve.nae2.mixin.upgrades.gregcircuit;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.helpers.DualityInterface;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.common.crafting.patterntransform.transformers.GregTechCircuitPatternTransformer;
import co.neeve.nae2.common.interfaces.IExtendedUpgradeInventory;
import co.neeve.nae2.common.items.NAEBaseItemUpgrade;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DualityInterface.class, remap = false)
public abstract class MixinDualityInterface {
	@Unique
	private final Object2IntOpenHashMap<ICraftingPatternDetails> nae2$cachedCircuitValues =
		new Object2IntOpenHashMap<>();
	
	@Shadow
	@Final
	private UpgradeInventory upgrades;

	@Shadow
	protected abstract void updateCraftingList();

	@Inject(
		method = "pushPattern",
		at = @At(
			value = "INVOKE",
			target = "Lappeng/helpers/DualityInterface;pushItemsOut(Lnet/minecraft/util/EnumFacing;)V",
			shift = At.Shift.BEFORE
		)
	)
	private void injectCircuitChange(ICraftingPatternDetails patternDetails, InventoryCrafting table,
	                                 CallbackInfoReturnable<Boolean> cir,
	                                 @Local(name = "te") TileEntity tileEntity) {
		if (this.upgrades instanceof IExtendedUpgradeInventory upgradeInventory
			&& upgradeInventory.getInstalledUpgrades(Upgrades.UpgradeType.GREGTECH_CIRCUIT) > 0) {

			// I'm sorry, Seni.
			if (tileEntity instanceof MetaTileEntityHolder metaTileEntityHolder) {
				var mte = metaTileEntityHolder.getMetaTileEntity();

				if (mte instanceof IGhostSlotConfigurable slotConfigurable && slotConfigurable.hasGhostCircuitInventory()) {
					slotConfigurable.setGhostCircuitConfig(
						this.nae2$cachedCircuitValues.getOrDefault(patternDetails, 0));
				}
			}
		}
	}

	@Inject(method = "onChangeInventory", at = @At("HEAD"))
	private void injectInventoryChange(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added,
	                                   CallbackInfo ci) {
		if ((added.getItem() instanceof NAEBaseItemUpgrade addUpgrade && addUpgrade.getType(added) == Upgrades.UpgradeType.GREGTECH_CIRCUIT)
			|| (removed.getItem() instanceof NAEBaseItemUpgrade remUpgrade && remUpgrade.getType(added) == Upgrades.UpgradeType.GREGTECH_CIRCUIT)) {
			this.updateCraftingList();
		}
	}

	@Inject(method = "updateCraftingList", at = @At("HEAD"))
	private void injectUpdateCraftingList(CallbackInfo ci) {
		this.nae2$cachedCircuitValues.clear();
	}

	@Inject(
		method = "addToCraftingList",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
			shift = At.Shift.BEFORE
		)
	)
	private void injectCraftingList(ItemStack is, CallbackInfo ci,
	                                @Local LocalRef<ICraftingPatternDetails> detailsRef) {
		if (this.upgrades instanceof IExtendedUpgradeInventory extendedUpgradeInventory
			&& extendedUpgradeInventory.getInstalledUpgrades(Upgrades.UpgradeType.GREGTECH_CIRCUIT) > 0) {
			var details = detailsRef.get();
			if (details != null && details.getInputs() != null) {
				var optCircuit = GregTechCircuitPatternTransformer.getCircuitValueFromDetails(details);

				optCircuit.ifPresent(integer -> this.nae2$cachedCircuitValues.put(details, integer.intValue()));
			}
		}
	}
}
