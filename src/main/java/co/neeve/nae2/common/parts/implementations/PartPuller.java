package co.neeve.nae2.common.parts.implementations;

import appeng.api.config.Upgrades;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.helpers.Puller;
import co.neeve.nae2.common.parts.NAEBasePartState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;

public class PartPuller extends NAEBasePartState implements IGridTickable, IAEAppEngInventory, IPullerHost,
	IItemHandler {

	public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/interface_base");

	@PartModels
	public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
		new ResourceLocation(AppEng.MOD_ID, "part/interface_off"));

	@PartModels
	public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
		new ResourceLocation(AppEng.MOD_ID, "part/interface_on"));

	@PartModels
	public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
		new ResourceLocation(AppEng.MOD_ID, "part/interface_has_channel"));

	private final Puller puller = new Puller(this);

	public PartPuller(final ItemStack is) {
		super(is);
		this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
	}

	@Override
	public void getBoxes(final IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 12, 11, 11, 14);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		this.puller.setTargets(EnumSet.of(this.getSide().getFacing()));
	}

	@Override
	public float getCableConnectionLength(AECableType cable) {
		return 4;
	}

	@Override
	public boolean onPartActivate(final EntityPlayer p, final EnumHand hand, final Vec3d pos) {
		if (Platform.isServer()) {
			NAE2.gui().openGUI(p, this.getHost().getTile(), this.getSide(),
				co.neeve.nae2.common.sync.GuiBridge.PULLER);
		}
		return true;
	}

	@Override
	public @NotNull TickingRequest getTickingRequest(final @NotNull IGridNode node) {
		return this.puller.getTickingRequest(node);
	}

	@Override
	public @NotNull TickRateModulation tickingRequest(final @NotNull IGridNode node, final int ticksSinceLastCall) {
		return this.puller.tickingRequest(node, ticksSinceLastCall);
	}

	@Override
	public @NotNull IPartModel getStaticModels() {
		if (this.isActive() && this.isPowered()) {
			return MODELS_HAS_CHANNEL;
		} else if (this.isPowered()) {
			return MODELS_ON;
		} else {
			return MODELS_OFF;
		}
	}

	@Override
	public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack,
	                              ItemStack itemStack1) {

	}

	@Override
	public Puller getPuller() {
		return this.puller;
	}

	@Override
	public void markForSave() {
		this.getHost().markForSave();
		this.getHost().markForUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capabilityClass) {
		return capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this
			: super.getCapability(capabilityClass);
	}

	@Override
	public boolean hasCapability(Capability<?> capabilityClass) {
		return capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
			|| super.hasCapability(capabilityClass);
	}

	@Override
	public int getSlots() {
		return this.puller.getInternalInventory().getSlots();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.puller.getInternalInventory().getStackInSlot(slot);
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return this.puller.getInternalInventory().insertItem(slot, stack, simulate);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return this.puller.getInternalInventory().extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.puller.getInternalInventory().getSlotLimit(slot);
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		this.puller.getDrops(drops);
	}

	@Override
	public void readFromNBT(final NBTTagCompound data) {
		super.readFromNBT(data);
		this.puller.readFromNBT(data);
	}

	@Override
	public void writeToNBT(final NBTTagCompound data) {
		super.writeToNBT(data);
		this.puller.writeToNBT(data);
	}

	@Override
	public IConfigManager getConfigManager() {
		return this.puller.getConfigManager();
	}

	@Override
	public int getInstalledUpgrades(Upgrades u) {
		return this.puller.getInstalledUpgrades(u);
	}

	@Override
	public IItemHandler getInventoryByName(String name) {
		return this.puller.getInventoryByName(name);
	}

	@Override
	public TileEntity getTileEntity() {
		return this.getHost().getTile();
	}
}