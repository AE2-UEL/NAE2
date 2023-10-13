package co.neeve.nae2.common.items.cells.vc;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import co.neeve.nae2.common.features.subfeatures.VoidCellFeatures;
import co.neeve.nae2.common.registries.Materials;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BaseStorageCellVoid<T extends IAEStack<T>> extends AEBaseItem implements ICellWorkbenchItem {
	public BaseStorageCellVoid() {
		this.setMaxStackSize(1);
	}

	public boolean isEditable(ItemStack is) {
		return true;
	}

	public IItemHandler getUpgradesInventory(ItemStack is) {
		return null;
	}

	public FuzzyMode getFuzzyMode(ItemStack is) {
		return FuzzyMode.IGNORE_ALL;
	}

	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {}

	public IItemHandler getConfigInventory(ItemStack is) {
		return new CellConfig(is);
	}

	@Nullable
	public ICellInventoryHandler<T> getCellInventory(ItemStack stack) {
		return AEApi.instance().registries().cell().getCellInventory(stack,
			null, this.getStorageChannel());
	}

	public abstract IStorageChannel<T> getStorageChannel();

	@Override
	protected void addCheckedInformation(ItemStack stack, World world, List<String> lines,
	                                     ITooltipFlag advancedTooltips) {
		super.addCheckedInformation(stack, world, lines, advancedTooltips);

		var empty = true;

		IMEInventoryHandler<?> inventory = getCellInventory(stack);
		if (inventory != null) {
			CellConfig cc = new CellConfig(stack);

			for (ItemStack is : cc) {
				if (!is.isEmpty()) {
					empty = false;
					lines.add(is.getDisplayName());
				}
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
				lines.add(I18n.format("nae2.storage_cell_void.count", this.getCondenserPower(stack),
					GuiText.Condenser.getLocal()));
			}
		}

	}

	public double getCondenserPower(ItemStack stack) {
		if (!VoidCellFeatures.CONDENSER_POWER.isEnabled()) return 0;

		var compound = stack.getTagCompound();
		if (compound == null) return 0;
		return compound.getDouble("power");
	}

	public void setCondenserPower(ItemStack stack, double power) {
		if (!VoidCellFeatures.CONDENSER_POWER.isEnabled()) return;

		NBTTagCompound compound = stack.getTagCompound();
		if (compound == null) stack.setTagCompound(compound = new NBTTagCompound());
		compound.setDouble("power", power);
	}

	public void addCondenserPowerFromInput(ItemStack stack, double power) {
		if (!VoidCellFeatures.CONDENSER_POWER.isEnabled()) return;

		this.setCondenserPower(stack,
			getCondenserPower(stack) + power / (double) getStorageChannel().transferFactor());
	}

	@Override
	public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
	                                                         @NotNull EnumHand hand) {
		this.disassembleDrive(player.getHeldItem(hand), player);
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	private void disassembleDrive(ItemStack stack, EntityPlayer player) {
		if (player.isSneaking()) {
			if (Platform.isClient()) {
				return;
			}

			InventoryPlayer playerInventory = player.inventory;
			if (playerInventory.getCurrentItem() == stack) {
				if (this.getCondenserPower(stack) < 1) {
					var ia = InventoryAdaptor.getAdaptor(player);
					playerInventory.setInventorySlotContents(playerInventory.currentItem, ItemStack.EMPTY);
					ItemStack extraB = ia.addItems(Materials.CELL_VOID_PART.getStack());
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

	protected void dropEmptyStorageCellCase(InventoryAdaptor ia, EntityPlayer player) {
		AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent((is) -> {
			ItemStack extraA = ia.addItems(is);
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
}
