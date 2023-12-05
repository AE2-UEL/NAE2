package co.neeve.nae2.common.interfaces;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.util.AEPartLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface INAEGuiItem<T extends IGuiItemObject> {
	T getGuiObject(ItemStack is, World w);

	default T getGuiObject(ItemStack is, World w, BlockPos bp, AEPartLocation side) {
		return this.getGuiObject(is, w);
	}
}
