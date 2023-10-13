package co.neeve.nae2.common.items;

import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import co.neeve.nae2.common.interfaces.INAEUpgradeHost;
import co.neeve.nae2.common.interfaces.INAEUpgradeModule;
import co.neeve.nae2.common.registries.Upgrades;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NAEBaseItemUpgrade extends AEBaseItem implements INAEUpgradeModule {
	public static NAEBaseItemUpgrade instance;

	public NAEBaseItemUpgrade() {
		this.setHasSubtypes(true);

		instance = this;
	}

	@Override
	public @NotNull String getTranslationKey(ItemStack itemStack) {
		return Upgrades.getByID(itemStack.getItemDamage()).getTranslationKey();
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		if (!this.isInCreativeTab(creativeTab)) return;

		for (Upgrades upgrade : Upgrades.values()) {
			if (!upgrade.isEnabled()) continue;
			
			itemStacks.add(new ItemStack(this, 1, upgrade.ordinal()));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal(Upgrades.getByID(stack.getItemDamage()).getTranslationKey());
	}

	@SideOnly(Side.CLIENT)
	public void addCheckedInformation(ItemStack stack, World world, List<String> lines,
	                                  ITooltipFlag advancedTooltips) {
		Upgrades u = this.getType(stack);
		if (u != null) {
			List<String> textList = new ArrayList<>();

			for (Map.Entry<ItemStack, Integer> j : u.getSupported().entrySet()) {
				String name = null;
				int limit = j.getValue();
				if (j.getKey().getItem() instanceof IItemGroup ig) {
					String str = ig.getUnlocalizedGroupName(u.getSupported().keySet(), j.getKey());
					if (str != null) {
						name = Platform.gui_localize(str) + (limit > 1 ? " (" + limit + ')' : "");
					}
				}

				if (name == null) {
					name = j.getKey().getDisplayName() + (limit > 1 ? " (" + limit + ')' : "");
				}

				if (!textList.contains(name)) {
					textList.add(name);
				}
			}

			lines.addAll(textList);
		}
	}

	public Upgrades getType(ItemStack stack) {
		return Upgrades.getByID(stack.getItemDamage());
	}

	@Override
	public @NotNull EnumActionResult onItemUseFirst(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
	                                                @NotNull EnumFacing side, float hitX, float hitY, float hitZ,
	                                                @NotNull EnumHand hand) {
		if (player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);
			IItemHandler upgrades = null;
			if (te instanceof IPartHost) {
				SelectedPart sp = ((IPartHost) te).selectPart(new Vec3d(hitX, hitY, hitZ));
				if (sp.part instanceof INAEUpgradeHost) {
					upgrades = ((ISegmentedInventory) sp.part).getInventoryByName("upgrades");
				}
			} else if (te instanceof INAEUpgradeHost) {
				upgrades = ((ISegmentedInventory) te).getInventoryByName("upgrades");
			}

			if (upgrades != null && !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof INAEUpgradeModule um) {
				Upgrades u = um.getType(player.getHeldItem(hand));
				if (u != null) {
					if (player.world.isRemote) {
						return EnumActionResult.PASS;
					}

					InventoryAdaptor ad = new AdaptorItemHandler(upgrades);
					player.setHeldItem(hand, ad.addItems(player.getHeldItem(hand)));
					return EnumActionResult.SUCCESS;
				}
			}
		}

		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}
}
