package co.neeve.nae2.common.registries;

import appeng.core.localization.GuiText;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.items.NAEBaseItemPart;
import co.neeve.nae2.common.parts.implementations.PartBeamFormer;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

public enum Parts {
	BEAM_FORMER("beam_former", PartBeamFormer.class, Features.BEAM_FORMERS),
	P2P_TUNNEL_INTERFACE("p2p_tunnel_interface", PartP2PInterface.class, GuiText.Interface, Features.INTERFACE_P2P) {
		@Override
		public String getTranslationKey() {
			return "p2p_tunnel";
		}
	};

	private final static HashMap<Class<? extends AEBasePart>, Parts> registered;

	static {
		registered = new HashMap<>();
		Arrays.stream(values()).forEach(x -> {
			registered.put(x.getClazz(), x);
		});
	}

	private final String id;
	private final Class<? extends AEBasePart> clazz;
	private final String translationKey;
	private Features feature;
	private GuiText extraName = null;

	private ModelResourceLocation modelResourceLocation = null;

	Parts(String id, Class<? extends AEBasePart> clazz) {
		this.id = id;
		this.clazz = clazz;
		this.translationKey = Tags.MODID + ".part." + this.id;
		if (Platform.isClientInstall()) {
			this.modelResourceLocation = new ModelResourceLocation(Tags.MODID + ":part/" + this.id, "inventory");
		}
	}

	Parts(String id, Class<? extends AEBasePart> clazz, Features feature) {
		this(id, clazz);
		this.feature = feature;
	}

	Parts(String id, Class<? extends AEBasePart> clazz, GuiText extraName) {
		this(id, clazz);

		this.extraName = extraName;
	}

	Parts(String id, Class<? extends AEBasePart> clazz, GuiText extraName, Features feature) {
		this(id, clazz, extraName);
		this.feature = feature;
	}

	public static Parts getByID(int id) {
		return values()[id];
	}

	@Nullable
	public static Parts getByName(String name) {
		name = name.toUpperCase();

		try {
			return Parts.valueOf(name);
		} catch (IllegalArgumentException err) {
			return null;
		}
	}

	public static int getPartID(AEBasePart part) {
		return registered.get(part.getClass()).ordinal();
	}

	public static String getTranslationKey(AEBasePart part) {
		return registered.get(part.getClass()).getTranslationKey();
	}

	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation() {
		return modelResourceLocation;
	}

	public String getTranslationKey() {
		return this.translationKey;
	}

	public String getId() {
		return id;
	}

	public Class<? extends AEBasePart> getClazz() {
		return clazz;
	}

	public AEBasePart newInstance(ItemStack partStack)
		throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
		return this.clazz.getDeclaredConstructor(ItemStack.class).newInstance(partStack);
	}

	@Nullable
	public GuiText getExtraName() {
		return extraName;
	}

	public boolean isEnabled() {
		return this.feature == null || this.feature.isEnabled();
	}

	public ItemStack getStack() {
		return NAEBaseItemPart.instance.getPartStack(this);
	}
}
