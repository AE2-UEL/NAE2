package co.neeve.nae2.common.integration.jei;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IRecipeCategoryWithOverlay {
	void drawOverlay(Minecraft minecraft, int offsetX, int offsetY, int mouseX, int mouseY);
}
