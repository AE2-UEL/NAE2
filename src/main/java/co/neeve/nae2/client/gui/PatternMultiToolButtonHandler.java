package co.neeve.nae2.client.gui;

import co.neeve.nae2.NAE2;
import co.neeve.nae2.client.gui.buttons.PatternMultiToolButton;
import co.neeve.nae2.client.gui.interfaces.IPatternMultiToolHostGui;
import co.neeve.nae2.common.enums.PatternMultiToolActionTypes;
import co.neeve.nae2.common.net.messages.PatternMultiToolPacket;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PatternMultiToolButtonHandler {
	private static void sendToServer(PatternMultiToolButton tb, SimpleNetworkWrapper network) {
		network.sendToServer(new PatternMultiToolPacket(PatternMultiToolActionTypes.BUTTON_PRESS,
			tb.getAction().ordinal()));
	}

	@SubscribeEvent
	public void handlePatternMultiToolPress(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if (event.getGui() instanceof IPatternMultiToolHostGui hostGui && event.getButton() instanceof PatternMultiToolButton tb) {
			var network = NAE2.net();

			switch (tb.getAction()) {
				case REPLACE -> {
					var obj = hostGui.getPMTObject();
					if (obj != null) {
						var replaceInventory = obj.getSearchReplaceInventory();
						var itemA = replaceInventory.getStackInSlot(0);
						var itemB = replaceInventory.getStackInSlot(1);

						if (itemB.isEmpty()) {
							var mc = event.getGui().mc;
							mc.displayGuiScreen(new GuiYesNo((boolean confirm, int i) -> {
								if (confirm) {
									sendToServer(tb, network);
								}
								mc.displayGuiScreen(event.getGui());
							}, "Are you sure you want to remove " +
								itemA.getDisplayName() + " from the patterns?", "This cannot be reversed!", 0));
						} else {
							sendToServer(tb, network);
						}
					}
				}
				case CLEAR -> {
					var mc = event.getGui().mc;

					mc.displayGuiScreen(new GuiYesNo((boolean confirm, int i) -> {
						if (confirm) {
							sendToServer(tb, network);
						}
						mc.displayGuiScreen(event.getGui());
					}, "Do you really want to clear patterns?", "This cannot be reversed!", 0));
				}
				default -> sendToServer(tb, network);
			}
		}
	}
}
