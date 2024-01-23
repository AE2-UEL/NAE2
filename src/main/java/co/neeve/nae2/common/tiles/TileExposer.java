package co.neeve.nae2.common.tiles;

import appeng.api.networking.GridFlags;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.common.helpers.exposer.ExposerBootstrapper;
import co.neeve.nae2.common.interfaces.IExposerHost;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class TileExposer extends AENetworkInvTile implements IExposerHost, ICapabilityProvider {
	@Nullable
	private final ExposerBootstrapper exposer;

	public TileExposer() {
		if (Platform.isServer()) {
			this.exposer = new ExposerBootstrapper(this, EnumSet.allOf(EnumFacing.class));
		} else {
			this.exposer = null;
		}

		this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(this);
	}

	@NotNull
	@Override
	public AECableType getCableConnectionType(@NotNull AEPartLocation aePartLocation) {
		return AECableType.SMART;
	}

	@NotNull
	@Override
	public IItemHandler getInternalInventory() {
		return EmptyHandler.INSTANCE;
	}

	@Override
	public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack,
	                              ItemStack itemStack1) {

	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
		return this.exposer != null && this.exposer.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
		return this.exposer != null && this.hasCapability(capability, facing)
			? this.exposer.getCapability(capability, facing)
			: null;
	}

	@Override
	public ExposerBootstrapper getExposerBootstrapper() {
		return this.exposer;
	}
}
