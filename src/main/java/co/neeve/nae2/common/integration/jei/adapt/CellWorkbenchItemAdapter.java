package co.neeve.nae2.common.integration.jei.adapt;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellWorkbenchItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class CellWorkbenchItemAdapter implements ICellWorkbenchItem {
	protected final ICellWorkbenchItem item;

	public CellWorkbenchItemAdapter(ICellWorkbenchItem item) {
		this.item = item;
	}

	@Override
	public boolean isEditable(ItemStack itemStack) {return this.item.isEditable(itemStack);}

	@Override
	public IItemHandler getUpgradesInventory(ItemStack itemStack) {return this.item.getUpgradesInventory(itemStack);}

	@Override
	public IItemHandler getConfigInventory(ItemStack itemStack) {return this.item.getConfigInventory(itemStack);}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack itemStack) {return this.item.getFuzzyMode(itemStack);}

	@Override
	public void setFuzzyMode(ItemStack itemStack, FuzzyMode fuzzyMode) {this.item.setFuzzyMode(itemStack, fuzzyMode);}
}
