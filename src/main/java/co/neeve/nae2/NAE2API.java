package co.neeve.nae2;

import co.neeve.nae2.common.api.ExposerAPI;
import co.neeve.nae2.common.api.TunnelConversionAPI;

public class NAE2API {
	private final ExposerAPI exposerAPI = new ExposerAPI();
	private final TunnelConversionAPI tunnelConversionAPI = new TunnelConversionAPI();

	NAE2API() {}

	/**
	 * Returns the Exposer API.
	 *
	 * @return Exposer API
	 */
	public ExposerAPI exposer() {
		return this.exposerAPI;
	}

	public TunnelConversionAPI tunnelConversion() {
		return this.tunnelConversionAPI;
	}
}
