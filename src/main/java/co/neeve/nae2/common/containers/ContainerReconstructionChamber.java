package co.neeve.nae2.common.containers;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerReconstructionChamber extends AEBaseContainer {

	public ContainerReconstructionChamber(InventoryPlayer ip, TileReconstructionChamber trc) {
		super(ip, trc);
		var ih = trc.getPrivateInv();

		this.addSlotToContainer(new AppEngSlot(ih, 0, 52, 37));
		this.addSlotToContainer(new AppEngSlot(ih, 1, 108, 37) {
			@Override
			public boolean isItemValid(@NotNull ItemStack par1ItemStack) {
				return false;
			}
		});
		this.bindPlayerInventory(ip, 0, 84);
	}
}
