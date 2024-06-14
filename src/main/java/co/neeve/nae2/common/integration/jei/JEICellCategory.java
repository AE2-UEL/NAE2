package co.neeve.nae2.common.integration.jei;

import appeng.api.AEApi;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.render.StackSizeRenderer;
import appeng.fluids.client.render.FluidStackSizeRenderer;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.Tags;
import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.ImmutableList;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.Internal;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class JEICellCategory implements IRecipeCategory<SingleStackRecipe>, IRecipeCategoryWithOverlay {
	public static final String UID = Tags.MODID + ":cell_view";
	public static final int CELL_SIZE = 18;
	public static final int SLOT_SIZE = CELL_SIZE - 2;
	private static final int WIDTH = 18 * 9 + 6;
	private static final int GRID_HEIGHT = 18 * 7;
	private static final int TOTAL_HEIGHT =
		GRID_HEIGHT + 6 + (int) ((Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 2) * 2 * 0.85);
	private final IDrawableStatic slotSprite;
	private final StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();
	private final FluidStackSizeRenderer fluidStackSizeRenderer = new FluidStackSizeRenderer();
	private final Int2ObjectOpenHashMap<ExtendedStackInfo<? extends IAEStack<?>>> extendedStacks =
		new Int2ObjectOpenHashMap<>();
	private final IDrawableStatic background;
	private final ObjectArrayList<Point> slotBackgroundXYs = new ObjectArrayList<>();
	private IDrawable icon = null;
	private CellInfo<? extends IAEStack<?>> cellInfo;

	public JEICellCategory(IJeiHelpers helpers) {
		var guiHelper = helpers.getGuiHelper();
		this.background = guiHelper.createBlankDrawable(WIDTH, TOTAL_HEIGHT);
		this.slotSprite = guiHelper.drawableBuilder(new ResourceLocation(Tags.MODID, "textures/gui/slot.png"), 0, 0,
				18, 18)
			.setTextureSize(18, 18)
			.build();

		AEApi.instance().definitions().items().cell64k().maybeStack(1).ifPresent((stack) -> this.icon =
			guiHelper.createDrawableIngredient(stack));
	}

	private static <T extends IAEStack<T>> IItemList<T> getAvailableItems(CellInfo<T> cellInfo) {
		return cellInfo.cellInv.getAvailableItems(cellInfo.channel.createList());
	}

	private static String getStorageChannelUnits(IStorageChannel<?> storageChannel) {
		if (storageChannel instanceof IItemStorageChannel) {
			return "items";
		} else if (storageChannel instanceof IFluidStorageChannel) {
			return "buckets";
		} else if (Platform.isModLoaded("mekeng") && storageChannel instanceof IGasStorageChannel) {
			return "buckets";
		} else {
			return "units";
		}
	}

	public static <T extends IAEStack<T>> CellInfo<T> getCellInfo(ItemStack is) {
		if (!(is.getItem() instanceof IStorageCell<?>)) return null;

		@SuppressWarnings("unchecked")
		var storageCell = (IStorageCell<T>) is.getItem();

		var handler = AEApi.instance().registries().cell().getHandler(is);
		if (handler == null) return null;

		var channel = storageCell.getChannel();
		var inventory = handler.getCellInventory(is, null, channel);
		if (inventory == null) return null;

		var cellInv = inventory.getCellInv();
		if (cellInv == null) return null;

		return new CellInfo<>(channel, cellInv);
	}

	@Override
	public @NotNull String getUid() {
		return UID;
	}

	@Override
	public @NotNull String getTitle() {
		return I18n.format("nae2.jei.cellview");
	}

	@Override
	public @NotNull String getModName() {
		return Tags.MODNAME;
	}

	@Override
	public @NotNull IDrawable getBackground() {
		return this.background;
	}

	@Nullable
	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayout recipeLayout, @NotNull SingleStackRecipe recipeWrapper,
	                      @NotNull IIngredients ingredients) {
		this.cellInfo = null;
		this.extendedStacks.clear();
		this.slotBackgroundXYs.clear();
		var stack = recipeWrapper.stack();
		this.cellInfo = getCellInfo(stack);
		if (this.cellInfo == null) return;

		// Collect all item stacks and sort, largest first.
		var storedStacks = new ArrayList<IAEStack<?>>();
		getAvailableItems(this.cellInfo).forEach(storedStacks::add);
		storedStacks.sort((a, b) -> Math.toIntExact(b.getStackSize() - a.getStackSize()));

		var totalTypes = (int) this.cellInfo.cellInv.getTotalItemTypes();
		var gridWidth = Math.min(9, totalTypes);
		var gridStartY = TOTAL_HEIGHT - GRID_HEIGHT;
		var gridStartX = WIDTH / 2 - gridWidth * 18 / 2;

		var stacks = recipeLayout.getItemStacks();

		var iter = storedStacks.iterator();
		for (var i = 0; i < totalTypes; i++) {
			var posX = gridStartX + (CELL_SIZE * (i % 9));
			var posY = gridStartY + (CELL_SIZE * (i / 9));

			if (iter.hasNext()) {
				var aeStack = iter.next();

				var renderer = Internal.getIngredientRegistry().getIngredientRenderer(stack);
				stacks.init(i, true, renderer, posX, posY, CELL_SIZE, CELL_SIZE,
					(this.slotSprite.getWidth() - SLOT_SIZE) / 2,
					(this.slotSprite.getHeight() - SLOT_SIZE) / 2);
				stacks.set(i, aeStack.asItemStackRepresentation());
				stacks.addTooltipCallback(this.getCallBack(this.cellInfo));

				this.extendedStacks.put(i, new ExtendedStackInfo<>(aeStack, posX, posY));
			}
			this.slotBackgroundXYs.add(new Point(posX, posY));
		}
	}

	@Override
	public void drawExtras(@NotNull Minecraft minecraft) {
		IRecipeCategory.super.drawExtras(minecraft);

		if (this.cellInfo != null) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.85, 0.85, 0.85);

			var offset = 3;
			var fontHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
			var format = NumberFormat.getInstance();

			var storedItemCount = this.cellInfo.cellInv.getStoredItemCount();
			var transferFactor = this.getTransferFactor();

			var capacity = (this.cellInfo.cellInv.getRemainingItemCount() + storedItemCount) / transferFactor;
			var byteLoss = this.cellInfo.cellInv.getBytesPerType() * this.cellInfo.cellInv.getStoredItemTypes();
			var capacityLoss = byteLoss * this.cellInfo.channel.getUnitsPerByte() / transferFactor;
			var unitName = I18n.format("nae2.jei.cellview." + getStorageChannelUnits(this.cellInfo.channel()));

			Minecraft.getMinecraft().fontRenderer.drawString(
				I18n.format("nae2.jei.cellview.stored",
					format.format(storedItemCount / transferFactor), format.format(capacity), unitName),
				offset, offset, 0x000000);

			Minecraft.getMinecraft().fontRenderer.drawString(
				I18n.format("nae2.jei.cellview.loss", format.format(capacityLoss), unitName),
				offset, offset + fontHeight + 2, 0x000000);

			GlStateManager.popMatrix();
			GlStateManager.color(1, 1, 1);

			for (var bgXY : this.slotBackgroundXYs) {
				this.slotSprite.draw(minecraft, bgXY.x, bgXY.y);
			}
		}
	}

	private int getTransferFactor() {
		final int transferFactor;
		if (Platform.isModLoaded("mekeng") && this.cellInfo.channel instanceof IGasStorageChannel) {
			transferFactor = 1000;
		} else {
			transferFactor = this.cellInfo.channel().transferFactor();
		}
		return transferFactor;
	}

	@Override
	public @NotNull List<String> getTooltipStrings(int mouseX, int mouseY) {
		if (this.cellInfo != null && mouseX > 0 && mouseY > 0 && mouseX < WIDTH && mouseY < TOTAL_HEIGHT - GRID_HEIGHT - 2) {
			var storedItemTypes = this.cellInfo.cellInv.getStoredItemTypes();
			var bytesPerType = this.cellInfo.cellInv.getBytesPerType();
			if (bytesPerType > 0 && storedItemTypes > 0) {
				var format = NumberFormat.getInstance();
				var byteLoss = bytesPerType * storedItemTypes;

				var builder = ImmutableList.<String>builder();
				builder.add(I18n.format("nae2.jei.cellview.hover.1", format.format(storedItemTypes)));
				builder.add(I18n.format("nae2.jei.cellview.hover.2"));
				builder.add("");
				builder.add(I18n.format("nae2.jei.cellview.hover.3",
					format.format(bytesPerType), format.format(storedItemTypes), format.format(byteLoss)));

				return builder.build();
			}
		}

		return Collections.emptyList();
	}

	@NotNull
	private <T> ITooltipCallback<T> getCallBack(CellInfo<? extends IAEStack<?>> cellInfo) {
		return (int slotIndex, boolean input, T ingredient, List<String> tooltip) -> {
			if (this.extendedStacks.containsKey(slotIndex)) {
				var format = NumberFormat.getInstance();
				var stackSize = this.extendedStacks.get(slotIndex).stack().getStackSize();
				var unitName = I18n.format("nae2.jei.cellview." + getStorageChannelUnits(cellInfo.channel()));

				tooltip.add(I18n.format("nae2.jei.cellview.hover.stored",
					format.format(stackSize / (double) this.getTransferFactor()), unitName));
				tooltip.add(I18n.format("nae2.jei.cellview.used",
					format.format(cellInfo.cellInv().getBytesPerType() + Math.ceil(stackSize / (double) cellInfo.channel().getUnitsPerByte()))));
			}
		};
	}

	@Override
	public void drawOverlay(Minecraft minecraft, int offsetX, int offsetY, int mouseX, int mouseY) {
		this.extendedStacks.forEach((key, info) -> {
			var stack = info.stack();

			if (stack instanceof IAEFluidStack aefs) {
				this.fluidStackSizeRenderer.renderStackSize(minecraft.fontRenderer, aefs, offsetX + info.posX(),
					offsetY + info.posY());
			} else {
				var aeStack = AEItemStack.fromItemStack(stack.asItemStackRepresentation());
				if (aeStack == null) return;
				aeStack.setStackSize(stack.getStackSize());
				this.stackSizeRenderer.renderStackSize(minecraft.fontRenderer,
					aeStack, offsetX + info.posX(), offsetY + info.posY());
			}
		});
	}

	@Desugar
	private record CellInfo<T extends IAEStack<T>>(IStorageChannel<T> channel, ICellInventory<T> cellInv) {}

	@Desugar
	private record ExtendedStackInfo<T extends IAEStack<T>>(IAEStack<T> stack, int posX, int posY) {}
}
