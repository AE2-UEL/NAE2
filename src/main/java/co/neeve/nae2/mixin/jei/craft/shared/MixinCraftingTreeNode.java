package co.neeve.nae2.mixin.jei.craft.shared;

import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.CraftingTreeProcess;
import co.neeve.nae2.common.interfaces.IExtendedCraftingTreeNode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = CraftingTreeNode.class, remap = false)
public class MixinCraftingTreeNode implements IExtendedCraftingTreeNode {
	@Shadow
	@Final
	private ICraftingGrid cc;

	@Shadow
	@Final
	private CraftingTreeProcess parent;

	@Shadow
	@Final
	private int slot;

	@Shadow
	@Final
	private World world;

	@Shadow
	@Final
	private ArrayList<CraftingTreeProcess> nodes;

	@Shadow
	@Final
	private CraftingJob job;

	@Shadow
	@Final
	private int depth;

	@Unique
	private ICraftingPatternDetails nae2$virtualPatternDetails;

	@Override
	public void setVirtualPatternDetails(ICraftingPatternDetails extras) {
		this.nae2$virtualPatternDetails = extras;
	}

	/**
	 * Override the tree nodes if we detect this is coming from a Virtual Pattern.
	 */
	@Inject(method = "addNode", at = @At(
		value = "INVOKE",
		target = "Lappeng/api/networking/crafting/ICraftingGrid;getCraftingFor" +
			"(Lappeng/api/storage/data/IAEItemStack;" +
			"Lappeng/api/networking/crafting/ICraftingPatternDetails;ILnet/minecraft/world/World;)" +
			"Lcom/google/common/collect/ImmutableCollection;"
	), cancellable = true)
	private void addNode(CallbackInfo ci) {
		if (this.nae2$virtualPatternDetails != null) {
			this.nodes.add(new CraftingTreeProcess(this.cc, this.job, this.nae2$virtualPatternDetails,
				(CraftingTreeNode) (Object) this, this.depth + 1));
			ci.cancel();
		}
	}
}
