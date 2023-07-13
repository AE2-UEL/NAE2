package co.neeve.nae2.mixin.client;

import appeng.util.item.AEItemStack;
import co.neeve.nae2.common.interfaces.IExtendedAEItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AEItemStack.class)
public class MixinAEItemStack implements IExtendedAEItemStack {
	private int extendedCount;

	@Override
	public int getExtendedCount() {
		return extendedCount;
	}

	@Override
	public void setExtendedCount(int count) {
		extendedCount = count;
	}
}
