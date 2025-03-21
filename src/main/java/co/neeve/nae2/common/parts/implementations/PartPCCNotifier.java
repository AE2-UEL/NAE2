package co.neeve.nae2.common.parts.implementations;

import appeng.parts.PartBasicState;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.item.ItemStack;

public class PartPCCNotifier extends PartBasicState {
	public PartPCCNotifier(ItemStack is) {
		super(is);
		this.getProxy().setIdlePowerUsage(1);
	}

	public void notifyMachine(int configNo) {
		var side = this.getSide();
		var blockPos = this.getTile().getPos();
		var tile = this.getTile()
			.getWorld().getTileEntity(blockPos.offset(side.getFacing()));

		if (tile instanceof MetaTileEntityHolder mteHolder) {
			var mte = mteHolder.getMetaTileEntity();
			if (mte instanceof IGhostSlotConfigurable slotConfigurable) {
				slotConfigurable.setGhostCircuitConfig(configNo);
			}
		}
	}
}
