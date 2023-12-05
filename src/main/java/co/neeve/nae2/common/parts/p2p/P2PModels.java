package co.neeve.nae2.common.parts.p2p;

import appeng.api.parts.IPartModel;
import appeng.parts.PartModel;
import co.neeve.nae2.Tags;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

class P2PModels {
	public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation("appliedenergistics2", "part/p2p" +
		"/p2p_tunnel_status_off");
	public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation("appliedenergistics2", "part/p2p" +
		"/p2p_tunnel_status_on");
	public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation("appliedenergistics2",
		"part" +
			"/p2p/p2p_tunnel_status_has_channel");
	public static final ResourceLocation MODEL_FREQUENCY = new ResourceLocation("appliedenergistics2", "part/builtin" +
		"/p2p_tunnel_frequency");
	private final IPartModel modelsOff;
	private final IPartModel modelsOn;
	private final IPartModel modelsHasChannel;

	public P2PModels(String frontModelPath) {
		var frontModel = new ResourceLocation(Tags.MODID, frontModelPath);
		this.modelsOff = new PartModel(MODEL_STATUS_OFF, MODEL_FREQUENCY, frontModel);
		this.modelsOn = new PartModel(MODEL_STATUS_ON, MODEL_FREQUENCY, frontModel);
		this.modelsHasChannel = new PartModel(MODEL_STATUS_HAS_CHANNEL, MODEL_FREQUENCY,
			frontModel);
	}

	public IPartModel getModel(boolean hasPower, boolean hasChannel) {
		if (hasPower && hasChannel) {
			return this.modelsHasChannel;
		} else {
			return hasPower ? this.modelsOn : this.modelsOff;
		}
	}

	public List<IPartModel> getModels() {
		List<IPartModel> result = new ArrayList<>();
		result.add(this.modelsOff);
		result.add(this.modelsOn);
		result.add(this.modelsHasChannel);
		return result;
	}
}
