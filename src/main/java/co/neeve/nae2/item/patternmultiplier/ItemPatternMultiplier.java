package co.neeve.nae2.item.patternmultiplier;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import co.neeve.nae2.NAE2;
import co.neeve.nae2.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemPatternMultiplier extends AEBaseItem implements IGuiItem {
    public ItemPatternMultiplier() {
        setRegistryName(Tags.MODID, "pattern_multiplier");
        setTranslationKey(Tags.MODID + ".pattern_multiplier");
        setMaxStackSize(1);
    }

    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World w, @NotNull EntityPlayer p, @NotNull EnumHand hand) {
        if (Platform.isServer()) {
            p.openGui(NAE2.instance, GuiHandlerPatternMultiplier.GuiIDs.PATTERN_MULTIPLIER.ordinal(), p.getEntityWorld(), p.inventory.currentItem, 0, 0);
        }

        p.swingArm(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
    }

    public IGuiItemObject getGuiObject(ItemStack is, World world, BlockPos pos) {
        return new ObjPatternMultiplier(is);
    }
}
