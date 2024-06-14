package co.neeve.nae2.common.items.cells.vc;

import appeng.api.config.*;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.items.contents.CellConfig;
import appeng.util.Platform;
import co.neeve.nae2.common.features.subfeatures.VoidCellFeatures;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

public class VoidCellInventory<T extends IAEStack<T>> implements ICellInventoryHandler<T> {
	protected final IStorageChannel<T> channel;
	protected final ItemStack itemStack;
	protected final IItemList<T> itemListCache;
	protected final VoidCell<T> item;
	protected final CellConfig cellConfig;
	protected final ISaveProvider saveProvider;
	protected boolean hasSticky;
	protected boolean hasInverter;
	protected boolean hasFuzzy;

	@SuppressWarnings("unchecked")
	public VoidCellInventory(ItemStack o, ISaveProvider iSaveProvider) {
		this.itemStack = o;

		this.item = (VoidCell<T>) o.getItem();
		this.channel = this.item.getStorageChannel();
		this.itemListCache = this.getChannel().createList();
		this.saveProvider = iSaveProvider;

		this.cellConfig = this.item.getCellConfig(o);
		for (final var is : this.cellConfig) {
			if (!is.isEmpty()) {
				var aeStack = this.item.handleConfigStack(is);
				if (aeStack != null) {
					this.itemListCache.add(aeStack);
				}
			}
		}

		var upgrades = this.item.getUpgradesInventory(this.itemStack);

		int x;
		ItemStack is;
		for (x = 0; x < upgrades.getSlots(); ++x) {
			is = upgrades.getStackInSlot(x);
			if (!is.isEmpty() && is.getItem() instanceof IUpgradeModule) {
				var u = ((IUpgradeModule) is.getItem()).getType(is);
				if (u != null) {
					switch (u) {
						case FUZZY -> this.hasFuzzy = true;
						case INVERTER -> this.hasInverter = true;
						case STICKY -> this.hasSticky = true;
					}
				}
			}
		}
	}

	@Override
	public T injectItems(T input, Actionable mode, IActionSource src) {
		if (!this.isValidInput(input)) return input;

		if (mode == Actionable.MODULATE) {
			this.item.addCondenserPowerFromInput(this.itemStack, input.getStackSize());
		}

		return null;
	}

	private boolean isValidInput(T input) {
		if (this.itemListCache.isEmpty()) {
			return true;
		}

		if (this.item.getUpgradesInventory(this.itemStack).getInstalledUpgrades(Upgrades.FUZZY) > 0) {
			return !this.itemListCache.findFuzzy(input, this.item.getFuzzyMode(this.itemStack)).isEmpty();
		} else {
			return this.itemListCache.findPrecise(input) != null;
		}
	}

	@Override
	public T extractItems(T request, Actionable mode, IActionSource src) {
		return null;
	}

	@Override
	public IItemList<T> getAvailableItems(IItemList<T> out) {
		return out;
	}

	@Override
	public IStorageChannel<T> getChannel() {
		return this.channel;
	}

	@Override
	public AccessRestriction getAccess() {
		return AccessRestriction.WRITE;
	}

	@Override
	public boolean isPrioritized(T input) {
		return this.itemListCache.isEmpty() || this.isValidInput(input);
	}

	@Override
	public boolean canAccept(T input) {
		return this.itemListCache.isEmpty() || this.isValidInput(input);
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public int getSlot() {
		return 0;
	}

	@Override
	public boolean validForPass(int i) {
		return true;
	}

	@Nullable
	@Override
	public ICellInventory<T> getCellInv() {
		return null;
	}

	@Override
	public boolean isPreformatted() {
		return !this.itemListCache.isEmpty();
	}

	public boolean isFuzzy() {
		return this.hasFuzzy;
	}

	public IncludeExclude getIncludeExcludeMode() {
		return this.hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST;
	}

	@Override
	public boolean isSticky() {
		return this.hasSticky;
	}

	public FuzzyMode getFuzzyMode() {
		var fz = Platform.openNbtData(this.itemStack).getString("FuzzyMode");

		try {
			return FuzzyMode.valueOf(fz);
		} catch (Throwable var4) {
			return FuzzyMode.IGNORE_ALL;
		}
	}

	public void setFuzzyMode(FuzzyMode fzMode) {
		Platform.openNbtData(this.itemStack).setString("FuzzyMode", fzMode.name());
	}

	public void addCondenserPowerFromInput(double power) {
		if (!VoidCellFeatures.CONDENSER_POWER.isEnabled()) return;

		this.setCondenserPower(this.getCondenserPower() + power / (double) this.getChannel().transferFactor());
	}

	public double getCondenserPower() {
		if (!VoidCellFeatures.CONDENSER_POWER.isEnabled()) return 0;

		var compound = this.itemStack.getTagCompound();
		if (compound != null) {
			return compound.getDouble("power");
		}

		return 0;
	}

	public void setCondenserPower(double power) {
		if (!VoidCellFeatures.CONDENSER_POWER.isEnabled()) return;

		var compound = this.itemStack.getTagCompound();
		if (compound == null) {
			this.itemStack.setTagCompound(compound = new NBTTagCompound());
		}
		compound.setDouble("power", power);
		if (this.saveProvider != null) {
			this.saveProvider.saveChanges(null);
		}
	}
}
