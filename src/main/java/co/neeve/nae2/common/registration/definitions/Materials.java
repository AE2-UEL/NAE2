package co.neeve.nae2.common.registration.definitions;

import appeng.api.definitions.IItemDefinition;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.IStackSrc;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.items.NAEMaterial;
import co.neeve.nae2.common.registration.registry.Registry;
import co.neeve.nae2.common.registration.registry.interfaces.DamagedDefinitions;
import co.neeve.nae2.common.registration.registry.rendering.DamagedItemRendering;
import co.neeve.nae2.common.registration.registry.rendering.IModelProvider;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("unused")
public class Materials implements DamagedDefinitions<DamagedItemDefinition, Materials.MaterialType> {
	private final Object2ObjectOpenHashMap<String, DamagedItemDefinition> byId =
		new Object2ObjectOpenHashMap<>();
	private final IItemDefinition cellPartVoid;
	private final IItemDefinition cellPart256K;
	private final IItemDefinition cellPart1024K;
	private final IItemDefinition cellPart4096K;
	private final IItemDefinition cellPart16384K;
	private final IItemDefinition cellFluidPart256K;
	private final IItemDefinition cellFluidPart1024K;
	private final IItemDefinition cellFluidPart4096K;
	private final IItemDefinition cellFluidPart16384K;
	private final IItemDefinition cellGasPart256K;
	private final IItemDefinition cellGasPart1024K;
	private final IItemDefinition cellGasPart4096K;
	private final IItemDefinition cellGasPart16384K;

	private final NAEMaterial material;

	public Materials(Registry registry) {
		this.material = new NAEMaterial();
		registry.item("material", () -> this.material)
			.rendering(new DamagedItemRendering<>(this))
			.build();

		this.cellPartVoid = this.createMaterial(this.material, MaterialType.CELL_PART_VOID);
		this.cellPart256K = this.createMaterial(this.material, MaterialType.CELL_PART_256K);
		this.cellPart1024K = this.createMaterial(this.material, MaterialType.CELL_PART_1024K);
		this.cellPart4096K = this.createMaterial(this.material, MaterialType.CELL_PART_4096K);
		this.cellPart16384K = this.createMaterial(this.material, MaterialType.CELL_PART_16384K);
		this.cellFluidPart256K = this.createMaterial(this.material, MaterialType.CELL_FLUID_PART_256K);
		this.cellFluidPart1024K = this.createMaterial(this.material, MaterialType.CELL_FLUID_PART_1024K);
		this.cellFluidPart4096K = this.createMaterial(this.material, MaterialType.CELL_FLUID_PART_4096K);
		this.cellFluidPart16384K = this.createMaterial(this.material, MaterialType.CELL_FLUID_PART_16384K);
		this.cellGasPart256K = this.createMaterial(this.material, MaterialType.CELL_GAS_PART_256K);
		this.cellGasPart1024K = this.createMaterial(this.material, MaterialType.CELL_GAS_PART_1024K);
		this.cellGasPart4096K = this.createMaterial(this.material, MaterialType.CELL_GAS_PART_4096K);
		this.cellGasPart16384K = this.createMaterial(this.material, MaterialType.CELL_GAS_PART_16384K);
	}

	public IItemDefinition cellGasPart256K() {
		return this.cellGasPart256K;
	}

	public IItemDefinition cellGasPart1024K() {
		return this.cellGasPart1024K;
	}

	public IItemDefinition cellGasPart4096K() {
		return this.cellGasPart4096K;
	}

	public IItemDefinition cellGasPart16384K() {
		return this.cellGasPart16384K;
	}

	@NotNull
	private DamagedItemDefinition createMaterial(NAEMaterial material, MaterialType materialType) {
		var def = new DamagedItemDefinition(materialType.getId(),
			material.createMaterial(materialType));

		this.byId.put(materialType.getId(), def);
		return def;
	}

	public Optional<MaterialType> getById(int itemDamage) {
		return Optional.ofNullable(MaterialType.getCachedValues().getOrDefault(itemDamage, null));
	}

	public Optional<DamagedItemDefinition> getById(String id) {
		return Optional.ofNullable(this.byId.getOrDefault(id, null));
	}

	@Override
	public Collection<MaterialType> getEntries() {
		return MaterialType.getCachedValues().values();
	}

	@Nullable
	@Override
	public MaterialType getType(ItemStack is) {
		return this.material.getTypeByStack(is);
	}

	public IItemDefinition cellPartVoid() {
		return this.cellPartVoid;
	}

	public IItemDefinition cellPart256K() {
		return this.cellPart256K;
	}

	public IItemDefinition cellPart1024K() {
		return this.cellPart1024K;
	}

	public IItemDefinition cellPart4096K() {
		return this.cellPart4096K;
	}

	public IItemDefinition cellPart16384K() {
		return this.cellPart16384K;
	}

	public IItemDefinition cellFluidPart256K() {
		return this.cellFluidPart256K;
	}

	public IItemDefinition cellFluidPart1024K() {
		return this.cellFluidPart1024K;
	}

	public IItemDefinition cellFluidPart4096K() {
		return this.cellFluidPart4096K;
	}

	public IItemDefinition cellFluidPart16384K() {
		return this.cellFluidPart16384K;
	}

	public enum MaterialType implements IModelProvider {
		CELL_PART_VOID("cell_part_void", Features.VOID_CELLS),
		CELL_PART_256K("cell_part_256k", Features.DENSE_CELLS),
		CELL_PART_1024K("cell_part_1024k", Features.DENSE_CELLS),
		CELL_PART_4096K("cell_part_4096k", Features.DENSE_CELLS),
		CELL_PART_16384K("cell_part_16384k", Features.DENSE_CELLS),
		CELL_FLUID_PART_256K("cell_part_fluid_256k", Features.DENSE_FLUID_CELLS),
		CELL_FLUID_PART_1024K("cell_part_fluid_1024k", Features.DENSE_FLUID_CELLS),
		CELL_FLUID_PART_4096K("cell_part_fluid_4096k", Features.DENSE_FLUID_CELLS),
		CELL_FLUID_PART_16384K("cell_part_fluid_16384k", Features.DENSE_FLUID_CELLS),
		CELL_GAS_PART_256K("cell_part_gas_256k", Features.DENSE_GAS_CELLS),
		CELL_GAS_PART_1024K("cell_part_gas_1024k", Features.DENSE_GAS_CELLS),
		CELL_GAS_PART_4096K("cell_part_gas_4096k", Features.DENSE_GAS_CELLS),
		CELL_GAS_PART_16384K("cell_part_gas_16384k", Features.DENSE_GAS_CELLS);

		private static Int2ObjectLinkedOpenHashMap<MaterialType> cachedValues;

		private final String id;
		private final Features features;
		private final String translationKey;
		private final ModelResourceLocation model;
		private boolean isRegistered;
		private int damageValue = this.ordinal();
		private Item itemInstance;
		private IStackSrc stackSrc;

		MaterialType(String id, Features features) {
			this.id = id;
			this.features = features;
			this.translationKey = "item." + Tags.MODID + ".material." + id;
			this.model = new ModelResourceLocation(new ResourceLocation(Tags.MODID, "material/" + id), "inventory");
		}

		public static Int2ObjectLinkedOpenHashMap<MaterialType> getCachedValues() {
			if (cachedValues == null) {
				cachedValues = new Int2ObjectLinkedOpenHashMap<>();
				Arrays.stream(values()).forEach((materialType -> cachedValues.put(materialType.ordinal(),
					materialType)));
			}
			return cachedValues;
		}

		public String getId() {
			return this.id;
		}

		public Features getFeature() {
			return this.features;
		}

		public String getTranslationKey() {
			return this.translationKey;
		}

		public ItemStack stack(final int size) {
			return new ItemStack(this.getItemInstance(), size, this.getDamageValue());
		}

		public boolean isRegistered() {
			return this.isRegistered;
		}

		@Override
		public boolean isEnabled() {
			return this.features.isEnabled();
		}

		public void markReady() {
			this.isRegistered = true;
		}

		public int getDamageValue() {
			return this.damageValue;
		}

		void setDamageValue(final int damageValue) {
			this.damageValue = damageValue;
		}

		public Item getItemInstance() {
			return this.itemInstance;
		}

		public void setItemInstance(final Item itemInstance) {
			this.itemInstance = itemInstance;
		}

		public MaterialStackSrc getStackSrc() {
			return (MaterialStackSrc) this.stackSrc;
		}

		public void setStackSrc(final MaterialStackSrc stackSrc) {
			this.stackSrc = stackSrc;
		}

		@Override
		public ModelResourceLocation getModel() {
			return this.model;
		}
	}

	public static class MaterialStackSrc implements IStackSrc {
		private final MaterialType src;
		private final boolean enabled;

		public MaterialStackSrc(MaterialType src, boolean enabled) {
			Preconditions.checkNotNull(src);
			this.src = src;
			this.enabled = enabled;
		}

		public ItemStack stack(int stackSize) {
			return this.src.stack(stackSize);
		}

		public Item getItem() {
			return this.src.getItemInstance();
		}

		public int getDamage() {
			return this.src.getDamageValue();
		}

		public boolean isEnabled() {
			return this.enabled;
		}
	}

}
