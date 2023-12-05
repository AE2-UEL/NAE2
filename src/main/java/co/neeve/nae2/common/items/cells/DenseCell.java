//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package co.neeve.nae2.common.items.cells;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import co.neeve.nae2.common.registration.definitions.Materials;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public abstract class DenseCell<T extends IAEStack<T>> extends AEBaseItem implements IStorageCell<T>,
	IItemGroup {
	private static final double LOG2 = Math.log(2);
	protected final Materials.MaterialType component;
	protected final int totalBytes;

	public DenseCell(Materials.MaterialType whichCell, int kilobytes) {
		this.setMaxStackSize(1);
		this.totalBytes = kilobytes * 1024;
		this.component = whichCell;
	}

	@SideOnly(Side.CLIENT)
	public void addCheckedInformation(ItemStack stack, World world, List<String> lines,
	                                  ITooltipFlag advancedTooltips) {
		AEApi.instance().client().addCellInformation(AEApi.instance().registries().cell().getCellInventory(stack,
			null, this.getChannel()), lines);
	}

	public int getBytes(@NotNull ItemStack cellItem) {
		return this.totalBytes;
	}

	public int getTotalTypes(@NotNull ItemStack cellItem) {
		return 63;
	}

	public boolean isBlackListed(@NotNull ItemStack cellItem, @NotNull T requestedAddition) {
		return false;
	}

	public boolean storableInStorageCell() {
		return false;
	}

	public boolean isStorageCell(@NotNull ItemStack i) {
		return true;
	}

	public String getUnlocalizedGroupName(Set<ItemStack> others, ItemStack is) {
		return GuiText.StorageCells.getUnlocalized();
	}

	public boolean isEditable(ItemStack is) {
		return true;
	}

	public IItemHandler getUpgradesInventory(ItemStack is) {
		return new CellUpgrades(is, 2);
	}

	public IItemHandler getConfigInventory(ItemStack is) {
		return new CellConfig(is);
	}

	public FuzzyMode getFuzzyMode(ItemStack is) {
		var fz = Platform.openNbtData(is).getString("FuzzyMode");

		try {
			return FuzzyMode.valueOf(fz);
		} catch (Throwable var4) {
			return FuzzyMode.IGNORE_ALL;
		}
	}

	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
		Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
	}

	public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
	                                                         @NotNull EnumHand hand) {
		this.disassembleDrive(player.getHeldItem(hand), player);
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	private boolean disassembleDrive(ItemStack stack, EntityPlayer player) {
		if (player.isSneaking()) {
			if (Platform.isClient()) {
				return false;
			}

			var playerInventory = player.inventory;
			IMEInventoryHandler<T> inv = AEApi.instance().registries().cell().getCellInventory(stack,
				null, this.getChannel());
			if (inv != null && playerInventory.getCurrentItem() == stack) {
				var ia = InventoryAdaptor.getAdaptor(player);
				var list = inv.getAvailableItems(this.getChannel().createList());
				if (list.isEmpty()) {
					playerInventory.setInventorySlotContents(playerInventory.currentItem, ItemStack.EMPTY);
					var extraB = ia.addItems(this.component.stack(1));
					if (!extraB.isEmpty()) {
						player.dropItem(extraB, false);
					}

					var upgradesInventory = this.getUpgradesInventory(stack);

					for (var upgradeIndex = 0; upgradeIndex < upgradesInventory.getSlots(); ++upgradeIndex) {
						var upgradeStack = upgradesInventory.getStackInSlot(upgradeIndex);
						var leftStack = ia.addItems(upgradeStack);
						if (!leftStack.isEmpty() && upgradeStack.getItem() instanceof IUpgradeModule) {
							player.dropItem(upgradeStack, false);
						}
					}

					this.dropEmptyStorageCellCase(ia, player);
					if (player.inventoryContainer != null) {
						player.inventoryContainer.detectAndSendChanges();
					}

					return true;
				}
			}
		}

		return false;
	}

	protected void dropEmptyStorageCellCase(final InventoryAdaptor ia, final EntityPlayer player) {
		AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent(is ->
		{
			final var extraA = ia.addItems(is);
			if (!extraA.isEmpty()) {
				player.dropItem(extraA, false);
			}
		});
	}

	public @NotNull EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world,
	                                                @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX
		, float hitY, float hitZ, @NotNull EnumHand hand) {
		return this.disassembleDrive(player.getHeldItem(hand), player) ? EnumActionResult.SUCCESS :
			EnumActionResult.PASS;
	}

	public @NotNull ItemStack getContainerItem(@NotNull ItemStack itemStack) {
		return AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1)
			.orElseThrow(() -> new MissingDefinitionException(
				"Tried to use empty storage cells while basic storage cells are defined."));
	}

	public boolean hasContainerItem(@NotNull ItemStack stack) {
		return AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_DISASSEMBLY_CRAFTING);
	}

	@Override
	public int getBytesPerType(@NotNull ItemStack itemStack) {
		return this.totalBytes / 128;
	}

	@Override
	public double getIdleDrain() {
		return Math.round(Math.log(this.totalBytes) / LOG2);
	}
}
