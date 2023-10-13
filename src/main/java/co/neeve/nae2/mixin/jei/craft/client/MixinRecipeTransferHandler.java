package co.neeve.nae2.mixin.jei.craft.client;

import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.integration.modules.jei.RecipeTransferHandler", remap = false)
public class MixinRecipeTransferHandler<T extends Container> {
	@Inject(method = "transferRecipe", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"
	))
	public void transferRecipe(T container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer,
	                           boolean doTransfer, CallbackInfoReturnable<IRecipeTransferError> cir,
	                           @Local(name = "recipe") NBTTagCompound recipe) {
		if (GuiScreen.isCtrlKeyDown()) {
			var compound = new NBTTagCompound();
			compound.setBoolean("craft", true);
			if (maxTransfer) compound.setBoolean("autoStart", true);
			recipe.setTag("nae2", compound);
		}
	}
}
