package co.neeve.nae2.client.rendering.helpers.beamformer.setups;

import gregtech.client.utils.BloomEffectUtil;

@SuppressWarnings({ "UnstableApiUsage", "deprecation" })
public class DeprecatedBeamBloomSetup extends BeamBloomSetup implements BloomEffectUtil.IBloomRenderFast {
	@Override
	public int customBloomStyle() {
		return 1;
	}
}
