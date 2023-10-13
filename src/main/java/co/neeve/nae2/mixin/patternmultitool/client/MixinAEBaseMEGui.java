package co.neeve.nae2.mixin.patternmultitool.client;

import appeng.client.gui.AEBaseMEGui;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AEBaseMEGui.class)
public class MixinAEBaseMEGui extends MixinAEBaseGui {
	public MixinAEBaseMEGui(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}
}
