package co.neeve.nae2;

import co.neeve.nae2.common.api.ExposerAPI;

public class NAE2API {
	private final ExposerAPI exposerAPI = new ExposerAPI();

	NAE2API() {}

	/**
	 * Returns the Exposer API.
	 *
	 * @return Exposer API
	 */
	public ExposerAPI exposer() {
		return this.exposerAPI;
	}
}
