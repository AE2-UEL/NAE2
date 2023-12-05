package co.neeve.nae2.common.tiles;

import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.common.net.messages.ReconstructorFXPacket;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.mod.items.lens.LensRecipeHandler;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import static de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI.RECONSTRUCTOR_LENS_CONVERSION_RECIPES;

public class TileReconstructionChamber extends AEBaseInvTile implements ITickable {
	private final AppEngInternalInventory inputInvInternal;
	private final WrapperFilteredItemHandler outputInv;
	private final WrapperChainedItemHandler publicInv;
	private final EnumMap<EnumFacing, Object> neighbors = new EnumMap<>(EnumFacing.class);
	private final AppEngInternalInventory outputInvInternal;
	private final WrapperChainedItemHandler internalInv;
	@SideOnly(Side.CLIENT)
	private LinkedList<Hologram> holograms;
	private int ticks;
	private ItemStack displayStack = ItemStack.EMPTY;
	private ItemStack holoStack = ItemStack.EMPTY;

	public TileReconstructionChamber() {
		if (Platform.isClient()) {
			this.holograms = new LinkedList<>();
		}

		this.outputInvInternal = new AppEngInternalInventory(this, 1, 512) {
			@Override
			protected int getStackLimit(int slot, @NotNull ItemStack stack) {
				return this.getSlotLimit(0);
			}
		};

		this.inputInvInternal = new AppEngInternalInventory(this, 1, 512) {
			@Override
			protected int getStackLimit(int slot, @NotNull ItemStack stack) {
				return this.getSlotLimit(0);
			}
		};

		var inputInv = new WrapperFilteredItemHandler(this.inputInvInternal,
			new IAEItemFilter() {
				@Override
				public boolean allowExtract(IItemHandler iItemHandler, int i, int i1) {
					return false;
				}

				@Override
				public boolean allowInsert(IItemHandler iItemHandler, int i, ItemStack itemStack) {
					return true;
				}
			});

		this.outputInv = new WrapperFilteredItemHandler(this.outputInvInternal, new IAEItemFilter() {
			@Override
			public boolean allowExtract(IItemHandler iItemHandler, int i, int i1) {
				return true;
			}

			@Override
			public boolean allowInsert(IItemHandler iItemHandler, int i, ItemStack itemStack) {
				return false;
			}
		});
		this.publicInv = new WrapperChainedItemHandler(inputInv, this.outputInv);
		this.internalInv = new WrapperChainedItemHandler(this.inputInvInternal, this.outputInvInternal);

		this.inputInvInternal.setFilter(new IAEItemFilter() {
			@Override
			public boolean allowExtract(IItemHandler iItemHandler, int i, int i1) {
				return true;
			}

			@Override
			public boolean allowInsert(IItemHandler iItemHandler, int i, ItemStack itemStack) {
				// Short circuit if possible.
				if (ItemStack.areItemStacksEqual(iItemHandler.getStackInSlot(i), itemStack)) return true;

				for (var recipe : RECONSTRUCTOR_LENS_CONVERSION_RECIPES) {
					if (recipe.getInput().apply(itemStack)) return true;
				}

				return false;
			}
		});
	}

	@Override
	protected void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);

		var stacks = new ItemStack[]{ this.displayStack, this.holoStack };
		for (var stack : stacks) {
			var aeis = AEItemStack.fromItemStack(stack);
			data.writeBoolean(aeis != null);
			if (aeis != null) {
				aeis.writeToPacket(data);
			}
		}
	}

	@Override
	public boolean canBeRotated() {
		return false;
	}

	@Override
	protected boolean readFromStream(ByteBuf data) throws IOException {
		var result = super.readFromStream(data);
		if (data.readBoolean()) {
			this.displayStack = AEItemStack.fromPacket(data).asItemStackRepresentation();
		} else {
			this.displayStack = ItemStack.EMPTY;
		}

		if (data.readBoolean()) {
			this.holoStack = AEItemStack.fromPacket(data).asItemStackRepresentation();
		} else {
			this.holoStack = ItemStack.EMPTY;
		}
		return result;
	}

	public IItemHandler getInputInvRaw() {
		return this.inputInvInternal;
	}

	@NotNull
	@Override
	public IItemHandler getInternalInventory() {
		return this.publicInv;
	}

	@Override
	public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack removed,
	                              ItemStack added) {
		if (!this.outputInv.getStackInSlot(0).isEmpty()) {
			this.tryEject();
		}

		this.sync();
	}

	private void sync() {
		var inputStack = this.inputInvInternal.getStackInSlot(0);
		var outputStack = this.outputInv.getStackInSlot(0);

		final ItemStack potentialStack;
		if (!inputStack.isEmpty()) {
			potentialStack = inputStack;
		} else {
			potentialStack = outputStack;
		}

		if (!potentialStack.isItemEqual(this.displayStack)) {
			this.updateDisplayStack(potentialStack);
			if (!outputStack.isEmpty()) {
				this.updateHoloStack(outputStack);
				return;
			} else if (!inputStack.isEmpty()) {
				var recipe = LensRecipeHandler.findMatchingRecipe(inputStack,
					ActuallyAdditionsAPI.lensDefaultConversion);
				if (recipe != null && recipe.getOutput() != null && !recipe.getOutput().isEmpty()) {
					this.updateHoloStack(recipe.getOutput());
					return;
				}
			}
			this.updateHoloStack(ItemStack.EMPTY);
		}
	}

	private void updateHoloStack(ItemStack is) {
		this.holoStack = is;
		if (!Platform.isClient()) {
			this.markForUpdate();
		}
	}

	private void updateDisplayStack(ItemStack is) {
		this.displayStack = is;
		if (!Platform.isClient()) {
			this.markForUpdate();
		}
	}


	public void handleConversionRecipe(IAtomicReconstructor reconstructor) {
		var input = this.inputInvInternal.getStackInSlot(0);
		var recipe = LensRecipeHandler.findMatchingRecipe(input, reconstructor.getLens());
		if (recipe != null && reconstructor.getEnergy() >= recipe.getEnergyUsed()) {
			var output = recipe.getOutput();
			if (!StackUtil.isValid(output)) return;

			var itemsPossible = Math.min(reconstructor.getEnergy() / recipe.getEnergyUsed(), input.getCount());
			if (itemsPossible > 0) {
				var outputCopy = output.copy();
				outputCopy.setCount(itemsPossible);

				var remaining = this.outputInvInternal.insertItem(0, outputCopy, false);
				if (remaining.getCount() != outputCopy.getCount()) {
					var inserted = outputCopy.getCount() - remaining.getCount();
					reconstructor.extractEnergy(recipe.getEnergyUsed() * inserted);
					input.shrink(inserted);

					NAE2.net().sendToAllAround(new ReconstructorFXPacket(this.pos),
						new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.pos.getX(),
							this.pos.getY(), this.pos.getZ(), 32));

					this.sync();
				}
			}
		}
	}

	@Override
	public void update() {
		this.ticks = (this.ticks + 1) % 20;
		if (this.ticks == 0 && !this.outputInv.getStackInSlot(0).isEmpty()) {
			if (this.tryEject()) this.ticks = 0;
		}
	}

	private boolean tryEject() {
		var is = this.outputInv.getStackInSlot(0);
		var originalCount = is.getCount();
		for (var facing : EnumFacing.values()) {
			is = this.pushTo(is, facing);
			if (is.isEmpty()) break;
		}

		this.outputInv.setStackInSlot(0, is);
		return is.isEmpty() || is.getCount() != originalCount;
	}

	private ItemStack pushTo(ItemStack output, EnumFacing d) {
		if (!output.isEmpty()) {
			var capability = this.neighbors.get(d);
			if (capability instanceof InventoryAdaptor adaptor) {
				var size = output.getCount();
				output = adaptor.addItems(output);
				var newSize = output.isEmpty() ? 0 : output.getCount();
				if (size != newSize) {
					this.saveChanges();
				}
			}

		}
		return output;
	}

	public void updateNeighbors() {
		var var1 = EnumFacing.VALUES;

		for (var f : var1) {
			var te = this.world.getTileEntity(this.pos.offset(f));
			Object capability = null;
			if (te != null) {
				capability = InventoryAdaptor.getAdaptor(te, f.getOpposite());
			}

			if (capability != null) {
				this.neighbors.put(f, capability);
			} else {
				this.neighbors.remove(f);
			}
		}
	}

	public void updateNeighbors(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
		EnumFacing updateFromFacing;
		if (pos.getX() != neighbor.getX()) {
			if (pos.getX() > neighbor.getX()) {
				updateFromFacing = EnumFacing.WEST;
			} else {
				updateFromFacing = EnumFacing.EAST;
			}
		} else if (pos.getY() != neighbor.getY()) {
			if (pos.getY() > neighbor.getY()) {
				updateFromFacing = EnumFacing.DOWN;
			} else {
				updateFromFacing = EnumFacing.UP;
			}
		} else {
			if (pos.getZ() == neighbor.getZ()) {
				return;
			}

			if (pos.getZ() > neighbor.getZ()) {
				updateFromFacing = EnumFacing.NORTH;
			} else {
				updateFromFacing = EnumFacing.SOUTH;
			}
		}

		if (pos.offset(updateFromFacing).equals(neighbor)) {
			var te = w.getTileEntity(neighbor);
			Object capability = null;
			if (te != null) {
				capability = InventoryAdaptor.getAdaptor(te, updateFromFacing.getOpposite());
			}

			if (capability != null) {
				this.neighbors.put(updateFromFacing, capability);
			} else {
				this.neighbors.remove(updateFromFacing);
			}
		}

	}

	@Override
	public boolean requiresTESR() {
		return true;
	}

	public ItemStack getDisplayStack() {
		return this.displayStack;
	}

	@SideOnly(Side.CLIENT)
	public void spawnHologram() {
		if (this.holoStack.isEmpty()) return;

		this.holograms.addFirst(new Hologram(this.holoStack, this.world.getTotalWorldTime()));
	}

	@SideOnly(Side.CLIENT)
	public List<Hologram> getHolograms() {
		return this.holograms;
	}


	@Override
	public void onLoad() {
		this.updateNeighbors();
	}

	public WrapperChainedItemHandler getPrivateInv() {
		return this.internalInv;
	}

	@SideOnly(Side.CLIENT)
	public static class Hologram {

		private final ItemStack holoStack;
		private final long life;

		public Hologram(ItemStack holoStack, long time) {
			this.holoStack = holoStack;
			this.life = time;
		}

		public static int getMaxLife() {
			return 30;
		}

		public double getStart() {
			return this.life;
		}

		public double getProgress(double time) {
			return Math.min(1, Math.max(0, (time - this.getStart()) / getMaxLife()));
		}

		public ItemStack getHoloStack() {
			return this.holoStack;
		}
	}
}
