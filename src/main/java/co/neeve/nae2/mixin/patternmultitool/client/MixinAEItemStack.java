package co.neeve.nae2.mixin.patternmultitool.client;

import appeng.util.item.AEItemStack;
import co.neeve.nae2.common.interfaces.IExtendedAEItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = AEItemStack.class)
public class MixinAEItemStack implements IExtendedAEItemStack {
	@Unique
	private int extendedCount;

	@Override
	public int getExtendedCount() {
		return this.extendedCount;
	}

	@Override
	public void setExtendedCount(int count) {
		this.extendedCount = count;
	}
}
