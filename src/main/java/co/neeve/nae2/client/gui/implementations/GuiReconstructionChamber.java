package co.neeve.nae2.client.gui.implementations;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import co.neeve.nae2.Tags;
import co.neeve.nae2.common.containers.ContainerReconstructionChamber;
import co.neeve.nae2.common.tiles.TileReconstructionChamber;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class GuiReconstructionChamber extends AEBaseGui {
	public GuiReconstructionChamber(InventoryPlayer inventoryPlayer, TileReconstructionChamber procurer) {
		super(new ContainerReconstructionChamber(inventoryPlayer, procurer));

		this.ySize = 166;
	}

	@Override
	public void drawFG(int i, int i1, int i2, int i3) {
		this.fontRenderer.drawString(I18n.format("tile.nae2.reconstruction_chamber.name"), 8, 6, 4210752);
		this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		var loc = new ResourceLocation(Tags.MODID, "textures/gui/reconstruction_chamber.png");
		this.mc.getTextureManager().bindTexture(loc);
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
	}
}
