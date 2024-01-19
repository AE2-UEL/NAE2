package co.neeve.nae2.common.parts.implementations;

import appeng.api.networking.GridFlags;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartBasicState;
import appeng.parts.PartModel;
import appeng.util.Platform;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.helpers.exposer.ExposerBootstrapper;
import co.neeve.nae2.common.interfaces.IExposerHost;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class PartExposer extends PartBasicState implements IExposerHost {
	public static final ResourceLocation MODEL_BASE = new ResourceLocation(Tags.MODID, "part/exposer");

	@PartModels
	private static final PartModel MODELS_OFF;

	@PartModels
	private static final PartModel MODELS_ON;

	@PartModels
	private static final PartModel MODELS_HAS_CHANNEL;

	static {
		MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation("appliedenergistics2",
			"part/interface_off"));
		MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation("appliedenergistics2",
			"part/interface_on"));
		MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation("appliedenergistics2",
			"part/interface_has_channel"));
	}

	@Nullable
	private ExposerBootstrapper exposer;
	private EnumFacing facing;

	public PartExposer(ItemStack is) {
		super(is);
		this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
	}

	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
		bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0);
	}

	@SideOnly(Side.CLIENT)
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

		if (Platform.isServer()) {
			this.facing = this.getSide().getFacing();
			this.exposer = new ExposerBootstrapper(this, EnumSet.of(this.facing));
			Platform.notifyBlocksOfNeighbors(this.getTile().getWorld(), this.getTile().getPos());
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capabilityClass) {
		return this.exposer != null ? this.exposer.getCapability(capabilityClass, this.facing) : null;
	}

	@Override
	public boolean hasCapability(Capability<?> capabilityClass) {
		return this.exposer != null && this.exposer.hasCapability(capabilityClass, this.facing);
	}

	@Override
	public ExposerBootstrapper getExposerBootstrapper() {
		return this.exposer;
	}
}
