package co.neeve.nae2.mixin.voidcell.condenser.shared;

import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileCondenser;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.common.interfaces.IExtendedTileCondenser;
import co.neeve.nae2.common.items.cells.vc.BaseStorageCellVoid;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileCondenser.class, remap = false)
public abstract class MixinTileCondenser extends AEBaseInvTile implements IExtendedTileCondenser {
	@Unique
	private final AppEngInternalInventory nae2$voidCell = new AppEngInternalInventory(this, 1);
	@Shadow
	@Final
	private AppEngInternalInventory outputSlot;
	@Shadow
	@Final
	private AppEngInternalInventory storageSlot;

	@Shadow
	public abstract double getStorage();

	@Shadow
	public abstract double getStoredPower();

	@Shadow
	protected abstract void setStoredPower(double storedPower);

	@Shadow
	public abstract void addPower(double rawPower);

	@Override
	public IItemHandler getVoidCellInv() {
		return this.nae2$voidCell;
	}

	@Inject(method = "writeToNBT", at = @At("RETURN"))
	public void writeToNBT(NBTTagCompound data, CallbackInfoReturnable<NBTTagCompound> cir) {
		this.nae2$voidCell.writeToNBT(data, "voidCellInv");
	}

	@Inject(method = "readFromNBT", at = @At("RETURN"))
	public void readFromNBT(NBTTagCompound data, CallbackInfo ci) {
		this.nae2$voidCell.readFromNBT(data, "voidCellInv");
	}

	/**
	 * Handle inventory changes.
	 * If a cell was inserted, fire a bogus addPower call to let AE2 know it has power.
	 */
	@Inject(method = "onChangeInventory", at = {
		@At("HEAD")
	}, cancellable = true)
	private void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added,
	                               CallbackInfo ci) {
		if (inv == this.nae2$voidCell && removed.isEmpty() && added.getItem() instanceof BaseStorageCellVoid) {
			this.addPower(0);
			ci.cancel();
		}
	}

	/**
	 * Handle power and output slot changes.
	 * Continuously refill the Condenser while the cell has power.
	 */
	@Inject(method = { "onChangeInventory", "addPower" }, at = {
		@At(
			value = "INVOKE",
			target = "Lappeng/tile/misc/TileCondenser;getRequiredPower()D"
		),
		@At(
			value = "INVOKE",
			target = "Lappeng/tile/misc/TileCondenser;addOutput(Lnet/minecraft/item/ItemStack;)V"
		)
	})
	private void onChangeInventory(CallbackInfo ci) {
		var is = this.getVoidCellInv().getStackInSlot(0);
		if (!is.isEmpty()) {
			this.refillFromVoidCell(is);
		}
	}

	/**
	 * Fixes https://github.com/PrototypeTrousers/Applied-Energistics-2/pull/316 for older versions of PAE2.
	 */
	@WrapOperation(method = { "onChangeInventory", "addPower" }, at = @At(
		value = "INVOKE",
		target = "Lappeng/tile/misc/TileCondenser;addOutput(Lnet/minecraft/item/ItemStack;)V"
	))
	private void bandAidFix(TileCondenser instance, ItemStack is, Operation<Void> operation,
	                        @Local(name = "output") ItemStack output, @Share("bandaid") LocalBooleanRef bandAidRef) {
		if (is == output && !bandAidRef.get())
			bandAidRef.set(true);

		operation.call(instance, bandAidRef.get() ? output.copy() : is);
	}

	@Unique
	private void refillFromVoidCell(ItemStack is) {
		if (is.getItem() instanceof BaseStorageCellVoid<?> cell) {
			var cellPower = cell.getCondenserPower(is);
			if (cellPower != 0) {
				var toAdd = this.getStorage() - this.getStoredPower();
				if (toAdd > 0) {
					var toBeAdded = Math.min(cellPower, toAdd);
					cell.setCondenserPower(is, cell.getCondenserPower(is) - toBeAdded);
					this.setStoredPower(this.getStoredPower() + toBeAdded);
					this.markForUpdate();
				}
			}
		}
	}
}
