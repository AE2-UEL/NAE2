package co.neeve.nae2.common.registries;

import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.parts.NAEBasePart;
import co.neeve.nae2.common.parts.implementations.PartBeamFormer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

public enum Parts {
	BEAM_FORMER("beam_former", PartBeamFormer.class);

	private final static HashMap<Class<? extends NAEBasePart>, Parts> registered;

	static {
		registered = new HashMap<>();
		Arrays.stream(values()).forEach(x -> {
			registered.put(x.getClazz(), x);
		});
	}

	private final String id;
	private final Class<? extends NAEBasePart> clazz;
	private final String translationKey;
	@SideOnly(Side.CLIENT)
	private ModelResourceLocation modelResourceLocation = null;

	Parts(String id, Class<? extends NAEBasePart> clazz) {
		this.id = id;
		this.clazz = clazz;
		this.translationKey = Tags.MODID + ".part." + this.id;
		if (Platform.isClient()) {
			this.modelResourceLocation = new ModelResourceLocation(Tags.MODID + ":part/" + this.id, "inventory");
		}

	}

	public static Parts getByID(int id) {
		return values()[id];
	}

	public static int getPartID(NAEBasePart part) {
		return registered.get(part.getClass()).ordinal();
	}

	public static String getTranslationKey(NAEBasePart part) {
		return registered.get(part.getClass()).getTranslationKey();
	}

	public ModelResourceLocation getModelResourceLocation() {
		return modelResourceLocation;
	}

	public String getTranslationKey() {
		return this.translationKey;
	}

	public String getId() {
		return id;
	}

	public Class<? extends NAEBasePart> getClazz() {
		return clazz;
	}

	public NAEBasePart newInstance(ItemStack partStack)
		throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
		return this.clazz.getDeclaredConstructor(ItemStack.class).newInstance(partStack);
	}
}
