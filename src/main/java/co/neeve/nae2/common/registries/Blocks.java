package co.neeve.nae2.common.registries;

import appeng.block.AEBaseTileBlock;
import appeng.tile.AEBaseTile;
import co.neeve.nae2.Tags;
import co.neeve.nae2.client.rendering.tesr.TESRReconstructionChamber;
import co.neeve.nae2.common.blocks.BlockReconstructionChamber;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.features.IFeature;
import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import de.ellpeck.actuallyadditions.mod.jei.reconstructor.ReconstructorRecipeCategory;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public enum Blocks {
	RECONSTRUCTION_CHAMBER("reconstruction_chamber", new BlockReconstructionChamber(),
		TileReconstructionChamber.class, Features.RECONSTRUCTION_CHAMBER) {
		@Override
		public Collection<String> getJEIDescription() {
			return Collections.singletonList("tile.nae2.reconstruction_chamber.desc");
		}

		@Override
		public void jeiRegister(IModRegistry registry) {
			super.jeiRegister(registry);

			registry.addRecipeCatalyst(this.getStack(), ReconstructorRecipeCategory.NAME);
		}

		@Override
		@SideOnly(Side.CLIENT)
		protected TileEntitySpecialRenderer<?> getTESR() {
			return new TESRReconstructionChamber();
		}
	};

	private final Block block;
	private final ItemBlock blockItem;
	private final ResourceLocation resourceLocation;
	private IFeature feature = null;

	<T extends Block> Blocks(String id, T block) {
		this.resourceLocation = new ResourceLocation(Tags.MODID, id);

		this.block = block;
		this.block.setTranslationKey(Tags.MODID + "." + id);
		this.block.setRegistryName(resourceLocation);
		this.block.setCreativeTab(CreativeTab.instance);

		this.blockItem = new ItemBlock(block);
		this.blockItem.setTranslationKey(Tags.MODID + "." + id);
		this.blockItem.setRegistryName(resourceLocation);
		this.blockItem.setCreativeTab(CreativeTab.instance);
	}

	<T extends AEBaseTileBlock, U extends AEBaseTile> Blocks(String id, T block, Class<U> teClass) {
		this(id, block);
		block.setTileEntity(teClass);
		GameRegistry.registerTileEntity(teClass, resourceLocation);

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			var tesr = this.getTESR();
			if (tesr != null) {
				//noinspection unchecked
				ClientRegistry.bindTileEntitySpecialRenderer(teClass, (TileEntitySpecialRenderer<? super U>) tesr);
			}
		}
	}

	<T extends AEBaseTileBlock, U extends AEBaseTile> Blocks(String id, T block, Class<U> teClass, IFeature feature) {
		this(id, block, teClass);

		this.feature = feature;
	}

	@SideOnly(Side.CLIENT)
	protected TileEntitySpecialRenderer<?> getTESR() {
		return null;
	}

	public void jeiRegister(IModRegistry registry) {
		var desc = this.getJEIDescription();
		if (desc != null) {
			registry.addIngredientInfo(this.getStack(), VanillaTypes.ITEM,
				desc.stream().map(I18n::format).toArray(String[]::new));
		}
	}

	@Nullable
	public Collection<String> getJEIDescription() {
		return null;
	}

	public Block getBlock() {
		return this.block;
	}

	public ItemBlock getItemBlock() {
		return this.blockItem;
	}

	public ItemStack getStack() {
		return new ItemStack(this.blockItem, 1);
	}

	public ResourceLocation getResourceLocation() {
		return this.resourceLocation;
	}

	public boolean isEnabled() {
		return this.feature != null && this.feature.isEnabled();
	}
}
