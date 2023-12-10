package co.neeve.nae2.common.items;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.features.ActivityState;
import appeng.core.features.ItemStackSrc;
import appeng.items.AEBaseItem;
import co.neeve.nae2.common.registration.definitions.Parts;
import com.github.bsideup.jabel.Desugar;
import com.google.common.base.Preconditions;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("rawtypes")
public class NAEBaseItemPart extends AEBaseItem implements IPartItem {
	private static final int INITIAL_REGISTERED_CAPACITY = Parts.PartType.values().length;
	private static final Comparator<Map.Entry<Integer, PartTypeWithVariant>> REGISTERED_COMPARATOR =
		new RegisteredComparator();
	public static NAEBaseItemPart instance;
	private final Map<Integer, PartTypeWithVariant> registered;

	public NAEBaseItemPart() {
		this.registered = new HashMap<>(INITIAL_REGISTERED_CAPACITY);
		this.setHasSubtypes(true);

		instance = this;
	}

	@Override
	public @NotNull String getTranslationKey(@NotNull ItemStack itemStack) {
		var type = this.getTypeByStack(itemStack);

		if (type == null) {
			return "item.nae2.invalid";
		}

		return type.getUnlocalizedName().toLowerCase();
	}

	@Nullable
	public Parts.PartType getTypeByStack(final @NotNull ItemStack is) {
		final var pt = this.registered.get(is.getItemDamage());
		if (pt != null) {
			return pt.part;
		}

		return null;
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		final List<Map.Entry<Integer, PartTypeWithVariant>> types = new ArrayList<>(this.registered.entrySet());
		types.sort(REGISTERED_COMPARATOR);

		for (final var part : types) {
			itemStacks.add(new ItemStack(this, 1, part.getKey()));
		}
	}

	@Nullable
	@Override
	public IPart createPartFromItemStack(ItemStack is) {
		final var type = this.getTypeByStack(is);
		if (type == null) {
			return null;
		}

		final var part = type.getPart();
		if (part == null) {
			return null;
		}

		try {
			if (type.getConstructor() == null) {
				type.setConstructor(part.getConstructor(ItemStack.class));
			}

			return type.getConstructor().newInstance(is);
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
		         IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public @NotNull String getItemStackDisplayName(final @NotNull ItemStack is) {
		final var pt = this.getTypeByStack(is);

		if (pt != null && pt.getExtraName() != null) {
			return super.getItemStackDisplayName(is) + " - " + pt.getExtraName().getLocal();
		}

		return super.getItemStackDisplayName(is);
	}

	public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World w, @NotNull BlockPos pos,
	                                           @NotNull EnumHand hand, @NotNull EnumFacing side, float hitX,
	                                           float hitY, float hitZ) {
		return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, w);
	}

	@Nonnull
	public final ItemStackSrc createPart(Parts.PartType mat) {
		Preconditions.checkNotNull(mat);
		return this.createPart(mat, 0);
	}

	public ItemStackSrc createPart(Parts.PartType partType, int varID) {
		assert partType != null;
		assert varID >= 0;

		// verify
		for (final var p : this.registered.values()) {
			if (p.part == partType && p.variant == varID) {
				throw new IllegalStateException("Cannot create the same material twice...");
			}
		}

		var enabled = partType.isEnabled();

		final var partDamage = partType.getBaseDamage() + varID;
		final var state = ActivityState.from(enabled);
		final var output = new ItemStackSrc(this, partDamage, state);

		final var pti = new PartTypeWithVariant(partType, varID);

		this.processMetaOverlap(enabled, partDamage, partType, pti);

		return output;
	}

	private void processMetaOverlap(final boolean enabled, final int partDamage, final Parts.PartType mat,
	                                final PartTypeWithVariant pti) {
		assert partDamage >= 0;
		assert mat != null;
		assert pti != null;

		final var registeredPartType = this.registered.get(partDamage);
		if (registeredPartType != null) {
			throw new IllegalStateException("Meta Overlap detected with type " + mat + " and damage " + partDamage +
				". Found " + registeredPartType + " there already.");
		}

		if (enabled) {
			this.registered.put(partDamage, pti);
		}
	}

	public int variantOf(final int itemDamage) {
		final var registeredPartType = this.registered.get(itemDamage);
		if (registeredPartType != null) {
			return registeredPartType.variant;
		}

		return 0;
	}

	@Desugar
	record PartTypeWithVariant(Parts.PartType part, int variant) {
		PartTypeWithVariant {
			assert part != null;
			assert variant >= 0;

		}

		@Override
		public String toString() {
			return "PartTypeWithVariant{" + "part=" + this.part + ", variant=" + this.variant + '}';
		}
	}

	private static final class RegisteredComparator implements Comparator<Map.Entry<Integer, PartTypeWithVariant>> {
		@Override
		public int compare(final Map.Entry<Integer, PartTypeWithVariant> o1,
		                   final Map.Entry<Integer, PartTypeWithVariant> o2) {
			final var string1 = o1.getValue().part.name();
			final var string2 = o2.getValue().part.name();
			final var comparedString = string1.compareTo(string2);

			if (comparedString == 0) {
				return Integer.compare(o1.getKey(), o2.getKey());
			}

			return comparedString;
		}
	}
}
