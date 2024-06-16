package co.neeve.nae2.common.parts.p2p.iface;

import appeng.api.networking.*;
import appeng.api.parts.IPartHost;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.IInterfaceHost;
import appeng.me.cache.P2PCache;
import co.neeve.nae2.common.helpers.inv.IDelegate;
import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import com.google.common.collect.MapMaker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

public class InterfaceTunnelGridCache implements IGridCache {
	private final IGrid grid;
	protected Map<Short, CapabilityContainer> containerMap = new MapMaker().weakValues().makeMap();
	protected WeakHashMap<PartP2PInterface, HashSet<Object>> tunnelCapCache = new WeakHashMap<>();

	public InterfaceTunnelGridCache(IGrid grid) {this.grid = grid;}

	public static boolean isInterface(Object te, EnumFacing facing) {
		if (te instanceof IInterfaceHost) return true;
		if (te instanceof IFluidInterfaceHost) return true;
		if (te instanceof PartP2PInterface) return true;

		if (te instanceof IPartHost iPartHost) {
			var part = iPartHost.getPart(facing);
			return isInterface(part, facing);
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static <T> T getCapability(TileEntity tile, Capability<T> capabilityType, EnumFacing facing) {
		if (tile.hasCapability(capabilityType, facing)) {
			var capability = tile.getCapability(capabilityType, facing);
			if (capability instanceof IDelegate<?> delegate) {
				capability = (T) delegate.getDelegate();
			}

			return capability;
		}

		return null;
	}

	@Nullable
	public CapabilityContainer getCapabilityCacheForFreq(short freq) {
		if (freq == 0) return null;

		if (!this.containerMap.containsKey(freq)) {
			var container = new CapabilityContainer();
			this.containerMap.put(freq, container);
			return container;
		}
		return this.containerMap.get(freq);
	}

	@Override
	public void onUpdateTick() {}

	@Override
	public void removeNode(@NotNull IGridNode iGridNode, @NotNull IGridHost iGridHost) {}

	@Override
	public void addNode(@NotNull IGridNode iGridNode, @NotNull IGridHost iGridHost) {}

	@Override
	public void onSplit(@NotNull IGridStorage iGridStorage) {}

	@Override
	public void onJoin(@NotNull IGridStorage iGridStorage) {}

	@Override
	public void populateGridStorage(@NotNull IGridStorage iGridStorage) {}

	public void updateTunnelNetwork(short freq) {
		if (freq == 0) return;

		var cache = this.getCapabilityCacheForFreq(freq);
		if (cache == null) return;

		var p2pCache = (P2PCache) this.grid.getCache(P2PCache.class);
		var inputs = p2pCache.getInputs(freq, PartP2PInterface.class);

		var itemHandlerList = new ArrayList<IItemHandler>();
		var fluidHandlerList = new ArrayList<IFluidHandler>();
		for (var input : inputs) {
			var tunnel = (PartP2PInterface) input;
			var tile = ((PartP2PInterface) input).getFacingTileEntity();
			var facing = tunnel.getFacing().getOpposite();

			var capSet = this.tunnelCapCache.computeIfAbsent(tunnel, k -> new HashSet<>());
			if (isInterface(tile, facing)) {
				Object cap;
				if ((cap = getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) != null) {
					if (cap != cache) {
						capSet.add(cap);
						itemHandlerList.add(((IItemHandler) cap));
					}
				}

				if ((cap = getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) != null) {
					if (cap != cache) {
						capSet.add(cap);
						fluidHandlerList.add(((IFluidHandler) cap));
					}
				}
			}
		}

		cache.setItemHandler(itemHandlerList);
		cache.setFluidHandler(fluidHandlerList);
	}

	public void onNeighborChanged(PartP2PInterface tunnel) {
		var newCaps = new HashSet<>();
		var tile = tunnel.getFacingTileEntity();
		var facing = tunnel.getFacing().getOpposite();
		var cache = this.getCapabilityCacheForFreq(tunnel.getFrequency());

		if (isInterface(tile, facing)) {
			Object cap;
			if ((cap = getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) != null) {
				if (cap != cache) {
					newCaps.add(cap);
				}
			}

			if ((cap = getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) != null) {
				if (cap != cache) {
					newCaps.add(cap);
				}
			}
		}

		var cacheEntry = this.tunnelCapCache.get(tunnel);
		if (cacheEntry == null || !cacheEntry.equals(newCaps)) {
			this.tunnelCapCache.put(tunnel, newCaps);
			this.updateTunnelNetwork(tunnel.getFrequency());
		}
	}
}
