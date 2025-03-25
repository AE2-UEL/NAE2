package co.neeve.nae2.mixin.upgrades.gregcircuit;

import appeng.api.networking.IGridHost;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.util.AEPartLocation;
import appeng.helpers.DualityInterface;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.common.crafting.patterntransform.PatternTransform;
import co.neeve.nae2.common.crafting.patterntransform.transformers.GregTechCircuitPatternTransformer;
import co.neeve.nae2.common.interfaces.IExtendedUpgradeInventory;
import co.neeve.nae2.common.parts.implementations.PartPCCNotifier;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.WeakHashMap;

@Mixin(value = DualityInterface.class, remap = false)
public abstract class MixinDualityInterface {
	@Unique
	private WeakHashMap<ICraftingPatternDetails, Integer> nae2$cachedCircuitValues = null;
	@Shadow
	@Final
	private UpgradeInventory upgrades;
	@Shadow
	private Set<ICraftingPatternDetails> craftingList;

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
	                                 @Local(name = "te") TileEntity tileEntity,
	                                 @Local(name = "s") EnumFacing s) {
		if (this.upgrades instanceof IExtendedUpgradeInventory upgradeInventory
			&& upgradeInventory.getInstalledUpgrades(Upgrades.UpgradeType.GREGTECH_CIRCUIT) > 0) {

			if (this.nae2$cachedCircuitValues == null) {
				this.nae2$cachedCircuitValues = new WeakHashMap<>();
			}
			int configNo = this.nae2$cachedCircuitValues.computeIfAbsent(patternDetails, d ->
				GregTechCircuitPatternTransformer.getCircuitValueFromDetails(d).orElse(0));

			// I'm sorry, Seni.
			if (tileEntity instanceof MetaTileEntityHolder metaTileEntityHolder) {
				var mte = metaTileEntityHolder.getMetaTileEntity();
				if (mte instanceof IGhostSlotConfigurable slotConfigurable && slotConfigurable.hasGhostCircuitInventory()) {
					slotConfigurable.setGhostCircuitConfig(configNo);
				}
			} else if (tileEntity instanceof IGridHost host) {
				var gridNode = host.getGridNode(AEPartLocation.fromFacing(s.getOpposite()));
				if (gridNode == null) {
					gridNode = host.getGridNode(AEPartLocation.INTERNAL);
				}

				if (gridNode != null) {
					var notifiers = gridNode.getGrid().getMachines(PartPCCNotifier.class);
					for (var notifier : notifiers) {
						((PartPCCNotifier) notifier.getMachine()).notifyMachine(configNo);
					}
				}
			}
		}
	}

	@Inject(method = "onChangeInventory", at = @At("HEAD"))
	private void injectInventoryChange(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added,
	                                   CallbackInfo ci) {
		if (PatternTransform.isTransformer(added) || PatternTransform.isTransformer(removed)) {
			if (inv == this.upgrades && this.upgrades instanceof IExtendedUpgradeInventory inventory) {
				this.craftingList = null;
				this.nae2$cachedCircuitValues = null;

				inventory.markDirty();
				this.updateCraftingList();
			}
		}
	}
}
