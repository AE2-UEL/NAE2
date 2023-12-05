package co.neeve.nae2.mixin.jei.craft.shared;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.PacketJEIRecipe;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemStackHashStrategy;
import co.neeve.nae2.common.helpers.VirtualPatternDetails;
import co.neeve.nae2.common.interfaces.IExtendedCraftingGridCache;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Mixin(value = PacketJEIRecipe.class, remap = false)
public class MixinPacketJEIRecipe {
	@Unique
	private boolean nae2$craft;

	@Unique
	private boolean nae2$autoStart;
	@Shadow
	private List<ItemStack[]> recipe;

	@Shadow
	private List<ItemStack> output;

	/**
	 * Finds the optimal ingredients for a given recipe.
	 *
	 * @param recipe   The crafting recipe.
	 * @param strategy The strategy for comparing ItemStacks.
	 * @return A map of optimal ingredients and their counts.
	 */
	@Unique
	private static <T extends ItemStack> Object2IntMap<T> findOptimalIngredients(List<Set<T>> recipe,
	                                                                             Hash.Strategy<T> strategy) {
		Object2IntMap<T> bestIngredients = new Object2IntOpenCustomHashMap<>(strategy);
		var bestIngredientCount = new int[]{ Integer.MAX_VALUE };
		backtrack(new Object2IntOpenCustomHashMap<>(strategy), recipe, 0, bestIngredients, bestIngredientCount);
		return bestIngredients;
	}

	/**
	 * Backtracks to find the optimal set of ingredients.
	 *
	 * @param currentIngredients  The current set of ingredients.
	 * @param recipe              The crafting recipe.
	 * @param recipeIndex         The current index in the recipe.
	 * @param bestIngredients     The best set of ingredients found so far.
	 * @param bestIngredientCount The smallest number of unique ingredients found so far.
	 */
	@Unique
	private static <T> void backtrack(Object2IntMap<T> currentIngredients, List<Set<T>> recipe,
	                                  int recipeIndex, Object2IntMap<T> bestIngredients, int[] bestIngredientCount) {
		if (recipeIndex == recipe.size()) {
			var currentIngredientCount = currentIngredients.size();
			if (currentIngredientCount < bestIngredientCount[0]) {
				bestIngredientCount[0] = currentIngredientCount;
				bestIngredients.clear();
				bestIngredients.putAll(currentIngredients);
			}
			return;
		}

		for (var ingredient : recipe.get(recipeIndex)) {
			currentIngredients.put(ingredient, currentIngredients.getOrDefault(ingredient, 0) + 1);
			backtrack(currentIngredients, recipe, recipeIndex + 1, bestIngredients, bestIngredientCount);
			var count = currentIngredients.getInt(ingredient) - 1;
			if (count == 0) {
				currentIngredients.removeInt(ingredient);
			} else {
				currentIngredients.put(ingredient, count);
			}
		}
	}

	@Inject(method = "<init>(Lio/netty/buffer/ByteBuf;)V", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/nbt/NBTTagCompound;getKeySet()Ljava/util/Set;",
		remap = true
	))
	private void ctor(ByteBuf stream, CallbackInfo ci, @Local NBTTagCompound comp) {
		if (comp == null) return;
		var nae2 = comp.getCompoundTag("nae2");
		this.nae2$craft = nae2.getBoolean("craft");
		this.nae2$autoStart = nae2.getBoolean("autoStart");
	}

	@Inject(method = "serverPacketData", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/inventory/Container;onCraftMatrixChanged(Lnet/minecraft/inventory/IInventory;)V",
		remap = true
	))
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player, CallbackInfo ci,
	                             @Local(name = "craftMatrix") IItemHandler craftMatrix,
	                             @Local ICraftingGrid crafting,
	                             @Local IGrid grid,
	                             @Local IContainerCraftingPacket cct) {
		// Not asked. No do.
		if (!this.nae2$craft) return;

		// No encoders.
		if (player.openContainer instanceof ContainerPatternEncoder) return;

		// Not a crafting recipe. No do.
		if (this.output.size() != 1) return;

		var outputAIS = AEItemStack.fromItemStack(this.output.get(0));

		// Invalid output stack. No do.
		if (outputAIS == null) return;

		// Not a valid context. No do.
		final var context = ((AEBaseContainer) cct).getOpenContext();
		if (context == null) return;

		// Collect all items from the recipe and compare.
		// Fill in whatever we're missing from the grid that's present in the recipe.
		var strategy = ItemStackHashStrategy.comparingAllButCount();

		var missingItems = new ArrayList<Set<ItemStack>>();
		for (var i = 0; i < craftMatrix.getSlots(); i++) {
			var stackInSlot = craftMatrix.getStackInSlot(i);
			if (stackInSlot.isEmpty()) {
				var recipe = new ObjectOpenCustomHashSet<>(strategy);
				var stacksInRecipeSlot = this.recipe.get(i);
				for (var stackInRecipeSlot : stacksInRecipeSlot) {
					if (!stackInRecipeSlot.isEmpty() &&
						crafting.getCraftingFor(AEItemStack.fromItemStack(stackInRecipeSlot),
							null,
							0,
							null).size() > 0) {
						recipe.add(stackInRecipeSlot);
					}
				}

				if (recipe.size() != 0) missingItems.add(recipe);
			}
		}

		// Nothing is missing? No do.
		if (missingItems.isEmpty()) return;

		// Find the optimal way of satisfying the recipe, minimizing the number of different
		// items while maximizing the stack size.
		// Upcast to AEItemStacks. Nothing should be null here, but who knows what AE2 may do.
		var optimal = findOptimalIngredients(missingItems, strategy)
			.entrySet().stream()
			.map(entry -> Objects.requireNonNull(AEItemStack.fromItemStack(entry.getKey()))
				.setStackSize(entry.getValue()))
			.collect(Collectors.toList());

		// Create a Virtual Pattern, despite AE2 telling us not to.
		var pattern = new VirtualPatternDetails(optimal,
			this.output.stream().map(AEItemStack::fromItemStack).collect(Collectors.toList()));

		// Try firing the crafting job.
		Future<ICraftingJob> futureJob = null;
		try {
			final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
			futureJob = ((IExtendedCraftingGridCache) cg).beginCraftingJobFromDetails(player.world, grid,
				cct.getActionSource(), outputAIS, pattern, null);

			final var te = context.getTile();
			if (te != null) {
				Platform.openGUI(player, te, context.getSide(), GuiBridge.GUI_CRAFTING_CONFIRM);
			} else {
				if (player.openContainer instanceof IInventorySlotAware i) {
					Platform.openGUI(player, i.getInventorySlot(), GuiBridge.GUI_CRAFTING_CONFIRM,
						i.isBaubleSlot());
				}
			}

			if (player.openContainer instanceof ContainerCraftConfirm ccc) {
				ccc.setAutoStart(this.nae2$autoStart);
				ccc.setJob(futureJob);
			} else {
				futureJob.cancel(true);
			}
		} catch (final Throwable e) {
			if (futureJob != null) {
				futureJob.cancel(true);
			}
			AELog.debug(e);
		}
	}
}
