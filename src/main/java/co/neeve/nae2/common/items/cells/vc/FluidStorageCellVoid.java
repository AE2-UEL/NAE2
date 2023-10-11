package co.neeve.nae2.common.items.cells.vc;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.helper.FluidCellConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class FluidStorageCellVoid extends BaseStorageCellVoid<IAEFluidStack> {
	@Override
	public IItemHandler getConfigInventory(ItemStack is) {
		return new FluidCellConfig(is);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation(ItemStack stack, World world, List<String> lines,
	                                  ITooltipFlag advancedTooltips) {
		super.addCheckedInformation(stack, world, lines, advancedTooltips);
	}

	@Override
	public IStorageChannel<IAEFluidStack> getStorageChannel() {
		return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
	}
}
