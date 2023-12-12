package co.neeve.nae2.client.gui.implementations;

import appeng.api.config.Settings;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import co.neeve.nae2.client.gui.buttons.NAEGuiImgButton;
import co.neeve.nae2.common.containers.ContainerPuller;
import co.neeve.nae2.common.parts.implementations.IPullerHost;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class GuiPuller extends GuiUpgradeable {

	public static final ResourceLocation LOC = new ResourceLocation("minecraft",
		"textures/gui/container/dispenser.png");
	private NAEGuiImgButton autoPushButton;

	public GuiPuller(InventoryPlayer inventoryPlayer, IPullerHost puller) {
		super(new ContainerPuller(inventoryPlayer, puller));

		this.ySize = ((ContainerPuller) this.inventorySlots).getHeight();
	}

	@Override
	public void drawFG(int i, int i1, int i2, int i3) {
		this.fontRenderer.drawString(I18n.format("tile.nae2.puller.name"), 8, 6, 4210752);
		this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

		if (this.autoPushButton != null) {
			this.autoPushButton.setButtonState(((ContainerPuller) this.cvb).getAutoPushMode());
		}
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		this.handleButtonVisibility();
		this.mc.getTextureManager().bindTexture(LOC);
		this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

	}


	@Override
	protected void addButtons() {
		super.addButtons();
		this.autoPushButton = new NAEGuiImgButton(this.guiLeft - 18,
			this.guiTop + 8, NAEGuiImgButton.ButtonType.AUTO_PUSH);
		this.buttonList.add(this.autoPushButton);
	}

	@Override
	protected void actionPerformed(final @NotNull GuiButton btn) throws IOException {
		super.actionPerformed(btn);

		final var backwards = Mouse.isButtonDown(1);

		if (btn == this.autoPushButton) {
			NetworkHandler
				.instance()
				.sendToServer(new PacketConfigButton(Settings.PLACE_BLOCK, backwards));
		}
	}
}