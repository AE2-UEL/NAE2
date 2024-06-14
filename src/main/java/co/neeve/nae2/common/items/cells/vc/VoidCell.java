package co.neeve.nae2.common.items.cells.vc;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.features.subfeatures.VoidCellFeatures;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;

public abstract class VoidCell<T extends IAEStack<T>> extends AEBaseItem implements ICellWorkbenchItem {
	private static final NumberFormat decimalFormat = NumberFormat.getInstance();

	public VoidCell() {
		this.setMaxStackSize(1);
	}

	@Override
	public boolean isEditable(ItemStack is) {
		return true;
	}

	@Override
	public CellUpgrades getUpgradesInventory(ItemStack is) {
		return new CellUpgrades(is, 2);
	}

	@Override
	public IItemHandler getConfigInventory(ItemStack is) {
		return this.getCellConfig(is);
	}

	public VoidCellInventory<T> getCellInventory(ItemStack stack) {
		return (VoidCellInventory<T>) AEApi.instance().registries().cell().getCellInventory(stack,
			null, this.getStorageChannel());
	}

	public abstract IStorageChannel<T> getStorageChannel();

	@SideOnly(Side.CLIENT)
	@Override
	protected void addCheckedInformation(ItemStack stack, World world, List<String> lines,
	                                     ITooltipFlag advancedTooltips) {
		super.addCheckedInformation(stack, world, lines, advancedTooltips);

		var empty = true;
		var inventory = this.getCellInventory(stack);
		if (inventory != null) {
			var cc = new CellConfig(stack);
			var storageList = new ObjectArrayList<String>();
			for (var is : cc) {
				if (!is.isEmpty()) {
					storageList.add(is.getDisplayName());
				}
			}

			if (!storageList.isEmpty()) {
				var list = (this.getIncludeExcludeMode(stack) == IncludeExclude.WHITELIST ? GuiText.Included :
					GuiText.Excluded).getLocal();
				if (this.isFuzzy(stack)) {
					lines.add("[" + GuiText.Partitioned.getLocal() + "] - " + list + ' ' + GuiText.Fuzzy.getLocal());
				} else {
					lines.add("[" + GuiText.Partitioned.getLocal() + "] - " + list + ' ' + GuiText.Precise.getLocal());
				}

				if (this.isSticky(stack)) {
					lines.add(GuiText.Sticky.getLocal());
				}

				lines.addAll(storageList);
				empty = false;
			}
		}

		if (empty) {
			lines.add(I18n.format("nae2.storage_cell_void.warning.1"));
			lines.add(I18n.format("nae2.storage_cell_void.warning.2",
				GuiText.CellWorkbench.getLocal()));
		}

		if (VoidCellFeatures.CONDENSER_POWER.isEnabled()) {
			var compound = stack.getTagCompound();
			if (compound != null) {
				lines.add("");
				lines.add(I18n.format("nae2.storage_cell_void.count",
					decimalFormat.format(this.getCondenserPower(stack)),
					GuiText.Condenser.getLocal()));
			}
		}

	}

	public boolean isSticky(ItemStack itemStack) {
		return this.getCellInventory(itemStack).isSticky();
	}

	public double getCondenserPower(ItemStack stack) {
		return this.getCellInventory(stack).getCondenserPower();
	}

	public void setCondenserPower(ItemStack stack, double power) {
		this.getCellInventory(stack).setCondenserPower(power);
	}

	public void addCondenserPowerFromInput(ItemStack stack, double power) {
		this.getCellInventory(stack).addCondenserPowerFromInput(power);
	}

	@Override
	public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
	                                                         @NotNull EnumHand hand) {
		this.disassembleDrive(player.getHeldItem(hand), player);
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	protected void disassembleDrive(ItemStack stack, EntityPlayer player) {
		if (player.isSneaking()) {
			if (Platform.isClient()) {
				return;
			}

			var playerInventory = player.inventory;
			if (playerInventory.getCurrentItem() == stack) {
				if (this.getCondenserPower(stack) < 1) {
					var ia = InventoryAdaptor.getAdaptor(player);
					playerInventory.setInventorySlotContents(playerInventory.currentItem, ItemStack.EMPTY);
					var extraB =
						ia.addItems(NAE2.definitions().materials().cellPartVoid().maybeStack(1).orElse(ItemStack.EMPTY));
					if (!extraB.isEmpty()) {
						player.dropItem(extraB, false);
					}

					this.dropEmptyStorageCellCase(ia, player);
					if (player.inventoryContainer != null) {
						player.inventoryContainer.detectAndSendChanges();
					}

				}
			}
		}
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is) {
		return this.getCellInventory(is).getFuzzyMode();
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
		this.getCellInventory(is).setFuzzyMode(fzMode);
	}

	protected void dropEmptyStorageCellCase(InventoryAdaptor ia, EntityPlayer player) {
		AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent((is) -> {
			var extraA = ia.addItems(is);
			if (!extraA.isEmpty()) {
				player.dropItem(extraA, false);
			}
		});
	}

	@Override
	public @NotNull ItemStack getContainerItem(final @NotNull ItemStack itemStack) {
		return AEApi.instance()
			.definitions()
			.materials()
			.emptyStorageCell()
			.maybeStack(1)
			.orElseThrow(() -> new MissingDefinitionException("Tried to use empty storage cells while basic storage " +
				"cells are defined."));
	}

	@Override
	public boolean hasContainerItem(final @NotNull ItemStack stack) {
		return AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_DISASSEMBLY_CRAFTING);
	}

	protected IncludeExclude getIncludeExcludeMode(ItemStack itemStack) {
		return this.getCellInventory(itemStack).getIncludeExcludeMode();
	}

	protected boolean isFuzzy(ItemStack itemStack) {
		return this.getCellInventory(itemStack).isFuzzy();
	}

	public abstract CellConfig getCellConfig(ItemStack o);

	@Nullable
	public abstract T handleConfigStack(ItemStack stack);
}
