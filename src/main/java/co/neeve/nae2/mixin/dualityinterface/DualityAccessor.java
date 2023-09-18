package co.neeve.nae2.mixin.dualityinterface;

import appeng.api.networking.IGrid;
import appeng.helpers.DualityInterface;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DualityInterface.class)
public interface DualityAccessor {
	@Accessor
	AENetworkProxy getGridProxy();

	@Invoker("sameGrid")
	boolean invokeSameGrid(IGrid grid) throws GridAccessException;
}
