package co.neeve.nae2.common.parts.implementations;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.items.parts.PartModels;
import appeng.parts.PartBasicState;
import appeng.parts.PartModel;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static appeng.parts.automation.PartLevelEmitter.*;

public class PartPCCNotifier extends PartBasicState {
	@PartModels
	public static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation(Tags.MODID,
		"part/pcc_notifier_base_off");
	@PartModels
	public static final ResourceLocation MODEL_BASE_ON = new ResourceLocation(Tags.MODID,
		"part/pcc_notifier_base_on");
	@PartModels

	public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
	public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
	public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
	public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
	public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
	public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

	public PartPCCNotifier(ItemStack is) {
		super(is);
		this.getProxy().setIdlePowerUsage(1);
	}

	@Override
	@NotNull
	public AECableType getCableConnectionType(final AEPartLocation dir) {
		return AECableType.SMART;
	}

	@Override
	public void getBoxes(final IPartCollisionHelper bch) {
		bch.addBox(7, 7, 11, 9, 9, 16);
	}

	public void notifyMachine(int configNo) {
		var side = this.getSide();
		var blockPos = this.getTile().getPos();
		var tile = this.getTile()
			.getWorld().getTileEntity(blockPos.offset(side.getFacing()));

		if (tile instanceof MetaTileEntityHolder mteHolder) {
			var mte = mteHolder.getMetaTileEntity();
			if (mte instanceof IGhostSlotConfigurable slotConfigurable) {
				slotConfigurable.setGhostCircuitConfig(configNo);
			}
		}
	}

	protected boolean isOn() {
		if (Platform.isClient()) {
			return (this.getClientFlags() & POWERED_FLAG) == POWERED_FLAG;
		}

		return this.getProxy().isActive();
	}

	@NotNull
	@Override
	public IPartModel getStaticModels() {
		if (this.isActive() && this.isPowered()) {
			return this.isOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
		} else if (this.isPowered()) {
			return this.isOn() ? MODEL_ON_ON : MODEL_OFF_ON;
		} else {
			return this.isOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
		}
	}

	@Override
	public float getCableConnectionLength(AECableType cable) {
		return 16;
	}
}
