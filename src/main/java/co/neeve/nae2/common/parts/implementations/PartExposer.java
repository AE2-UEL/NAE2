package co.neeve.nae2.common.parts.implementations;

import appeng.api.networking.GridFlags;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.parts.PartBasicState;
import co.neeve.nae2.common.helpers.exposer.Exposer;
import co.neeve.nae2.common.interfaces.IExposerHost;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

import static appeng.parts.misc.PartInterface.*;

public class PartExposer extends PartBasicState implements IExposerHost {
	public static final ResourceLocation MODEL_BASE = new ResourceLocation("appliedenergistics2",
		"part/interface_base");
	private Exposer exposer;
	private EnumFacing facing;

	public PartExposer(ItemStack is) {
		super(is);
		this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
	}

	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
		bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0);
	}

	public @NotNull IPartModel getStaticModels() {
		if (this.isActive() && this.isPowered()) {
			return MODELS_HAS_CHANNEL;
		} else {
			return this.isPowered() ? MODELS_ON : MODELS_OFF;
		}
	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		this.facing = this.getSide().getFacing();
		this.exposer = new Exposer(this, EnumSet.of(this.facing));
	}

	@Override
	public <T> T getCapability(Capability<T> capabilityClass) {
		return this.exposer != null ? this.exposer.getCapability(capabilityClass, this.facing) : null;
	}

	@Override
	public boolean hasCapability(Capability<?> capabilityClass) {
		return this.exposer != null && this.exposer.hasCapability(capabilityClass, this.facing);
	}
}
