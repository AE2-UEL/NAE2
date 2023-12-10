package co.neeve.nae2.common.items;

import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.core.features.IStackSrc;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.interfaces.INAEUpgradeHost;
import co.neeve.nae2.common.interfaces.INAEUpgradeModule;
import co.neeve.nae2.common.registration.definitions.Upgrades;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NAEBaseItemUpgrade extends AEBaseItem implements INAEUpgradeModule {
	public static NAEBaseItemUpgrade instance;
	private final Int2ObjectOpenHashMap<Upgrades.UpgradeType> dmgToUpgrade = new Int2ObjectOpenHashMap<>();

	public NAEBaseItemUpgrade() {
		this.setHasSubtypes(true);

		instance = this;
	}

	@Override
	public @NotNull String getTranslationKey(ItemStack itemStack) {
		var upgrade = NAE2.definitions().upgrades().getById(itemStack.getItemDamage());
		if (upgrade.isPresent()) {
			return upgrade.get().getTranslationKey();
		}

		return "item.nae2.invalid";
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		if (!this.isInCreativeTab(creativeTab)) return;

		for (var upgrade : Upgrades.UpgradeType.getCachedValues().values()) {
			if (!upgrade.isRegistered()) continue;

			itemStacks.add(new ItemStack(this, 1, upgrade.ordinal()));
		}
	}

	@SideOnly(Side.CLIENT)
	public void addCheckedInformation(ItemStack stack, World world, List<String> lines,
	                                  ITooltipFlag advancedTooltips) {
		var u = this.getType(stack);
		if (u != null) {
			List<String> textList = new ArrayList<>();

			for (var j : u.getSupported().entrySet()) {
				String name = null;
				int limit = j.getValue();
				if (j.getKey().getItem() instanceof IItemGroup ig) {
					var str = ig.getUnlocalizedGroupName(u.getSupported().keySet(), j.getKey());
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

	public @Nullable Upgrades.UpgradeType getType(ItemStack stack) {
		return NAE2.definitions().upgrades().getById(stack.getItemDamage()).orElse(null);
	}

	@Override
	public @NotNull EnumActionResult onItemUseFirst(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
	                                                @NotNull EnumFacing side, float hitX, float hitY, float hitZ,
	                                                @NotNull EnumHand hand) {
		if (player.isSneaking()) {
			var te = world.getTileEntity(pos);
			IItemHandler upgrades = null;
			if (te instanceof IPartHost) {
				var sp = ((IPartHost) te).selectPart(new Vec3d(hitX, hitY, hitZ));
				if (sp.part instanceof INAEUpgradeHost) {
					upgrades = ((ISegmentedInventory) sp.part).getInventoryByName("upgrades");
				}
			} else if (te instanceof INAEUpgradeHost) {
				upgrades = ((ISegmentedInventory) te).getInventoryByName("upgrades");
			}

			if (upgrades != null && !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof INAEUpgradeModule um) {
				var u = um.getType(player.getHeldItem(hand));
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

	public IStackSrc createUpgrade(Upgrades.UpgradeType upgradeType) {
		Preconditions.checkState(!upgradeType.isRegistered(), "Cannot create the same material twice.");

		var enabled = upgradeType.isEnabled();

		upgradeType.setStackSrc(new co.neeve.nae2.common.registration.definitions.Upgrades.UpgradeStackSrc(upgradeType,
			enabled));

		if (enabled) {
			upgradeType.setItemInstance(this);
			upgradeType.markReady();
			final var newUpgradeNum = upgradeType.getDamageValue();

			if (this.dmgToUpgrade.get(newUpgradeNum) == null) {
				this.dmgToUpgrade.put(newUpgradeNum, upgradeType);
			} else {
				throw new IllegalStateException("Meta Overlap detected.");
			}
		}

		return upgradeType.getStackSrc();
	}
}
