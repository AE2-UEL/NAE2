package co.neeve.nae2;

import co.neeve.nae2.common.helpers.exposer.ExposerAPI;

public class API {
	private final ExposerAPI exposerAPI = new ExposerAPI();

	API() {

	}

	/**
	 * Returns the Exposer API.
	 *
	 * @return Exposer API
	 */
	public ExposerAPI exposer() {
		return this.exposerAPI;
	}
}
