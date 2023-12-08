package co.neeve.nae2.mixin.voidcell.condenser.shared;

import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCondenser;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileCondenser;
import co.neeve.nae2.common.interfaces.IExtendedTileCondenser;
import co.neeve.nae2.common.items.cells.vc.BaseStorageCellVoid;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerCondenser.class)
public class MixinContainerTileCondenser extends AEBaseContainer {
	public MixinContainerTileCondenser(InventoryPlayer ip, TileEntity myTile, IPart myPart) {
		super(ip, myTile, myPart);
	}

	@Inject(method = "<init>", at = @At(
		value = "INVOKE",
		target = "Lappeng/container/implementations/ContainerCondenser;bindPlayerInventory" +
			"(Lnet/minecraft/entity/player/InventoryPlayer;II)V",
		remap = false
	))
	private void ctor(InventoryPlayer ip, TileCondenser condenser, CallbackInfo ci, @Local IItemHandler inv) {
		var slotRestrictedInput =
			new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.STORAGE_CELLS,
				((IExtendedTileCondenser) condenser).getVoidCellInv(), 0, 101, 52 + 26, ip) {
				@Override
				public boolean isItemValid(ItemStack i) {
					return i.getItem() instanceof BaseStorageCellVoid<?>;
				}
			};
		this.addSlotToContainer(slotRestrictedInput.setStackLimit(1));
	}
}
