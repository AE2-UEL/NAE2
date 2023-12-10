package co.neeve.nae2.common.items;


import appeng.core.features.IStackSrc;
import appeng.items.AEBaseItem;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.registration.definitions.Materials;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class NAEMaterial extends AEBaseItem {
	public static NAEMaterial instance;
	private final Int2ObjectOpenHashMap<Materials.MaterialType> dmgToMaterial = new Int2ObjectOpenHashMap<>();

	public NAEMaterial() {
		this.setHasSubtypes(true);

		instance = this;
	}

	@Override
	public @NotNull String getTranslationKey(ItemStack itemStack) {
		var material = NAE2.definitions().materials().getById(itemStack.getItemDamage());
		if (material.isPresent()) {
			return material.get().getTranslationKey();
		}

		return "item.nae2.invalid";
	}

	@Override
	protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
		if (!this.isInCreativeTab(creativeTab)) return;

		for (var material : Materials.MaterialType.getCachedValues().values()) {
			if (!material.isRegistered()) continue;

			itemStacks.add(new ItemStack(this, 1, material.ordinal()));
		}
	}

	public IStackSrc createMaterial(Materials.MaterialType materialType) {
		Preconditions.checkState(!materialType.isRegistered(), "Cannot create the same material twice.");

		var enabled = materialType.isEnabled();

		materialType.setStackSrc(new Materials.MaterialStackSrc(materialType, enabled));

		if (enabled) {
			materialType.setItemInstance(this);
			materialType.markReady();
			final var newMaterialNum = materialType.getDamageValue();

			if (this.dmgToMaterial.get(newMaterialNum) == null) {
				this.dmgToMaterial.put(newMaterialNum, materialType);
			} else {
				throw new IllegalStateException("Meta Overlap detected.");
			}
		}

		return materialType.getStackSrc();
	}

	public @Nullable Materials.MaterialType getTypeByStack(final ItemStack is) {
		return this.dmgToMaterial.get(is.getItemDamage());
	}
}
