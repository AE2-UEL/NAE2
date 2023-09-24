package co.neeve.nae2.common.items;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.items.AEBaseItem;
import co.neeve.nae2.common.registries.Parts;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("rawtypes")
public class NAEBaseItemPart extends AEBaseItem implements IPartItem {
	public static NAEBaseItemPart instance;

	public NAEBaseItemPart() {
		this.setHasSubtypes(true);

		instance = this;
	}

	@Override
	public @NotNull String getTranslationKey(ItemStack itemStack) {
		return Parts.getByID(itemStack.getItemDamage()).getTranslationKey();
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		if (!this.isInCreativeTab(creativeTab)) return;

		for (Parts part : Parts.values()) {
			itemStacks.add(new ItemStack(this, 1, part.ordinal()));
		}
	}

	@Nullable
	@Override
	public IPart createPartFromItemStack(ItemStack is) {
		try {
			return Parts.values()[MathHelper.clamp(
				is.getItemDamage(), 0, Parts.values().length - 1)]
				.newInstance(is);
		} catch (IllegalAccessException | NoSuchMethodException | InstantiationException |
		         InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public @NotNull String getItemStackDisplayName(ItemStack stack) {
		Parts pt = Parts.getByID(stack.getItemDamage());
		String name;
		if (pt.getTranslationKey().equals("p2p_tunnel")) {
			name = I18n.format("item.appliedenergistics2.multi_part." + pt.getTranslationKey() + ".name");
		} else {
			name = super.getItemStackDisplayName(stack);
		}

		return pt.getExtraName() != null ?
			name + " - " + pt.getExtraName().getLocal() :
			name;
	}

	public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World w, @NotNull BlockPos pos,
	                                           @NotNull EnumHand hand, @NotNull EnumFacing side, float hitX,
	                                           float hitY, float hitZ) {
		return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, w);
	}
}
