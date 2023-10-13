package co.neeve.nae2.common.interfaces;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.world.World;

import java.util.concurrent.Future;

public interface IExtendedCraftingGridCache {
	Future<ICraftingJob> beginCraftingJobFromDetails(World world, IGrid grid, IActionSource actionSrc,
	                                                 IAEItemStack slotItem, ICraftingPatternDetails details,
	                                                 ICraftingCallback cb);
}
