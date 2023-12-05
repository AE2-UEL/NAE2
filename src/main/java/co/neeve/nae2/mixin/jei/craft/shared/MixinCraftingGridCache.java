package co.neeve.nae2.mixin.jei.craft.shared;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingJob;
import appeng.me.cache.CraftingGridCache;
import co.neeve.nae2.common.interfaces.IExtendedCraftingGridCache;
import co.neeve.nae2.common.interfaces.IExtendedCraftingTreeNode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Mixin(value = CraftingGridCache.class, remap = false)
public abstract class MixinCraftingGridCache implements IExtendedCraftingGridCache {
	@Shadow
	@Final
	private static ExecutorService CRAFTING_POOL;

	@Override
	@Unique
	public Future<ICraftingJob> beginCraftingJobFromDetails(World world, IGrid grid, IActionSource actionSrc,
	                                                        IAEItemStack slotItem, ICraftingPatternDetails extras,
	                                                        ICraftingCallback cb) {
		if (world != null && grid != null && actionSrc != null && slotItem != null) {
			var job = new CraftingJob(world, grid, actionSrc, slotItem, cb);
			((IExtendedCraftingTreeNode) job.getTree()).setVirtualPatternDetails(extras);
			return CRAFTING_POOL.submit(job, job);
		} else {
			throw new IllegalArgumentException("Invalid Crafting Job Request");
		}
	}
}
