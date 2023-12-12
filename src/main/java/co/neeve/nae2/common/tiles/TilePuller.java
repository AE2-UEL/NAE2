package co.neeve.nae2.common.tiles;

import appeng.api.config.Upgrades;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.common.helpers.Puller;
import co.neeve.nae2.common.parts.implementations.IPullerHost;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class TilePuller extends AENetworkInvTile implements IGridTickable, IPullerHost {


	private final Puller puller = new Puller(this);

	public TilePuller() {
		this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
		this.puller.setTargets(EnumSet.allOf(EnumFacing.class));
	}

	@NotNull
	@Override
	public IItemHandler getInternalInventory() {
		return this.puller.getInternalInventory();
	}

	@Override
	public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack,
	                              ItemStack itemStack1) {

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
	public TickingRequest getTickingRequest(@NotNull IGridNode iGridNode) {
		return this.puller.getTickingRequest(iGridNode);
	}

	@NotNull
	@Override
	public TickRateModulation tickingRequest(@NotNull IGridNode iGridNode, int i) {
		return this.puller.tickingRequest(iGridNode, i);
	}

	@Override
	public Puller getPuller() {
		return this.puller;
	}

	@Override
	public void markForSave() {
		this.saveChanges();
	}

	@Override
	public IConfigManager getConfigManager() {
		return this.puller.getConfigManager();
	}

	@Override
	public int getInstalledUpgrades(Upgrades upgrades) {
		return this.puller.getInstalledUpgrades(upgrades);
	}

	@Override
	public IItemHandler getInventoryByName(String s) {
		return this.puller.getInventoryByName(s);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		this.puller.writeToNBT(data);
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.puller.readFromNBT(data);
	}

	@Override
	public TileEntity getTileEntity() {
		return this;
	}
}
