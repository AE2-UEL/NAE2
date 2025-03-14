package co.neeve.nae2.common.registration.definitions;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.parts.IPart;
import appeng.bootstrap.components.IInitComponent;
import appeng.core.Api;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.features.IFeature;
import co.neeve.nae2.common.items.NAEBaseItemPart;
import co.neeve.nae2.common.parts.implementations.PartBeamFormer;
import co.neeve.nae2.common.parts.implementations.PartExposer;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import co.neeve.nae2.common.parts.p2p.iface.InterfaceTunnelGridCache;
import co.neeve.nae2.common.registration.registry.Registry;
import co.neeve.nae2.common.registration.registry.helpers.PartModelsHelper;
import co.neeve.nae2.common.registration.registry.interfaces.Definitions;
import co.neeve.nae2.common.registration.registry.interfaces.IDefinition;
import co.neeve.nae2.common.registration.registry.rendering.ItemPartRendering;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.*;

public class Parts implements Definitions<DamagedItemDefinition> {
	private final Object2ObjectOpenHashMap<String, DamagedItemDefinition> byId = new Object2ObjectOpenHashMap<>();
	private final NAEBaseItemPart itemPart;
	private final DamagedItemDefinition beamFormer;
	private final DamagedItemDefinition p2pTunnelInterface;
	private final DamagedItemDefinition exposer;

	public Parts(Registry registry) {
		this.itemPart = new NAEBaseItemPart();
		registry.item("part", () -> this.itemPart)
			.rendering(new ItemPartRendering(this.itemPart))
			.build();

		// Register all part models
		var partModels = AEApi.instance().registries().partModels();
		for (var partType : PartType.values()) {
			partModels.registerModels(partType.getModels());
		}

		this.beamFormer = this.createPart(this.itemPart, PartType.BEAM_FORMER);
		this.p2pTunnelInterface = this.createPart(this.itemPart, PartType.P2P_TUNNEL_INTERFACE);
		this.p2pTunnelInterface.maybeStack(1)
			.ifPresent((tunnelStack) -> registry.addBootstrapComponent((IInitComponent) (r) -> {
				AEApi.instance().registries().gridCache()
					.registerGridCache(InterfaceTunnelGridCache.class, InterfaceTunnelGridCache.class);

				var tunnelType = AEApi.instance().registries().p2pTunnel()
					.registerTunnelType("NAE2_IFACE_P2P", tunnelStack);

				var definitions = Api.INSTANCE.definitions();

				definitions.blocks().iface().maybeStack(1)
					.ifPresent((stack) -> registerTunnelConversion(tunnelType, stack));

				definitions.parts().iface().maybeStack(1)
					.ifPresent((stack) -> registerTunnelConversion(tunnelType, stack));
			}));

		this.exposer = this.createPart(this.itemPart, PartType.EXPOSER);
	}

	private static void registerTunnelConversion(TunnelType tunnelType, ItemStack stack) {
		AEApi.instance().registries().p2pTunnel().addNewAttunement(stack, tunnelType);
	}

	public static Optional<PartType> getById(int itemDamage) {
		return Optional.ofNullable(PartType.getCachedValues().getOrDefault(itemDamage, null));
	}

	@NotNull
	private DamagedItemDefinition createPart(NAEBaseItemPart baseItemPart, PartType partType) {
		var def = new DamagedItemDefinition(partType.getId(),
			baseItemPart.createPart(partType));

		this.byId.put(partType.id, def);
		return def;
	}

	public DamagedItemDefinition getBeamFormer() {
		return this.beamFormer;
	}

	public DamagedItemDefinition p2pTunnelInterface() {
		return this.p2pTunnelInterface;
	}

	public DamagedItemDefinition exposer() {
		return this.exposer;
	}

	@Override
	public Optional<DamagedItemDefinition> getById(String id) {
		return Optional.ofNullable(this.byId.getOrDefault(id, null));
	}

	public enum PartType implements IDefinition {
		BEAM_FORMER("beam_former", PartBeamFormer.class, Features.BEAM_FORMERS),
		P2P_TUNNEL_INTERFACE("p2p_tunnel_interface",
			PartP2PInterface.class, Features.INTERFACE_P2P, GuiText.Interface) {
			@Override
			public String getUnlocalizedName() {
				return "item.appliedenergistics2.multi_part.p2p_tunnel";
			}
		},
		EXPOSER("exposer", PartExposer.class, Features.EXPOSER) {
			@Override
			public void addCheckedInformation(ItemStack stack, World world, List<String> lines,
			                                  ITooltipFlag advancedTooltips) {
				lines.add(I18n.format("nae2.exposer.tooltip"));
			}
		};

		private static Int2ObjectLinkedOpenHashMap<PartType> cachedValues;
		private final String id;
		private final Class<? extends IPart> clazz;
		private final int baseDamage;
		private final boolean enabled;
		private final Set<ResourceLocation> models;
		private Constructor<? extends IPart> constructor;
		private GuiText extraName;
		private List<ModelResourceLocation> itemModels;

		PartType(String id, Class<? extends IPart> clazz, IFeature features) {
			this.id = id;
			this.clazz = clazz;
			this.baseDamage = this.ordinal();

			this.enabled = features.isEnabled();
			if (this.enabled) {
				// Only load models if the part is enabled, otherwise we also run into class-loading issues while
				// scanning for annotations
				if (Platform.isClientInstall()) {
					this.itemModels = this.createItemModels(id);
				}
				if (clazz != null) {
					this.models = new HashSet<>(PartModelsHelper.createModels(clazz));
				} else {
					this.models = Collections.emptySet();
				}
			} else {
				if (Platform.isClientInstall()) {
					this.itemModels = Collections.emptyList();
				}
				this.models = Collections.emptySet();
			}
		}

		PartType(String id, Class<? extends IPart> clazz, Features features, GuiText extraName) {
			this(id, clazz, features);
			this.extraName = extraName;
		}

		public static Int2ObjectLinkedOpenHashMap<PartType> getCachedValues() {
			if (cachedValues == null) {
				cachedValues = new Int2ObjectLinkedOpenHashMap<>();
				Arrays.stream(values()).forEach((partType -> cachedValues.put(partType.ordinal(), partType)));
			}
			return cachedValues;
		}

		@SideOnly(Side.CLIENT)
		private static ModelResourceLocation modelFromBaseName(String baseName) {
			return new ModelResourceLocation(new ResourceLocation(Tags.MODID, "part/" + baseName), "inventory");
		}

		@SideOnly(Side.CLIENT)
		private List<ModelResourceLocation> createItemModels(String baseName) {
			return ImmutableList.of(modelFromBaseName(baseName));
		}

		public boolean isEnabled() {
			return this.enabled;
		}

		public int getBaseDamage() {
			return this.baseDamage;
		}

		public Class<? extends IPart> getPart() {
			return this.clazz;
		}

		public String getUnlocalizedName() {
			return "item.nae2.part." + this.name().toLowerCase();
		}

		public GuiText getExtraName() {
			return this.extraName;
		}

		public Constructor<? extends IPart> getConstructor() {
			return this.constructor;
		}

		public void setConstructor(final Constructor<? extends IPart> constructor) {
			this.constructor = constructor;
		}

		@SideOnly(Side.CLIENT)
		public List<ModelResourceLocation> getItemModels() {
			return this.itemModels;
		}

		public Set<ResourceLocation> getModels() {
			return this.models;
		}

		public String getId() {
			return this.id;
		}

		public void addCheckedInformation(ItemStack stack, World world, List<String> lines,
		                                  ITooltipFlag advancedTooltips) {
		}
	}
}
