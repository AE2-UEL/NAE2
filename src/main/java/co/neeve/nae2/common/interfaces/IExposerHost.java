package co.neeve.nae2.common.interfaces;

import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.IGridProxyable;
import co.neeve.nae2.common.helpers.exposer.ExposerBootstrapper;
import org.jetbrains.annotations.Nullable;

public interface IExposerHost extends IGridProxyable, IActionHost {
	@Nullable("Available on the server only")
	ExposerBootstrapper getExposerBootstrapper();
}
