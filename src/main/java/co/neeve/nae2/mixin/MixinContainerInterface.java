package co.neeve.nae2.mixin;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.parts.IPart;
import appeng.container.implementations.ContainerInterface;
import appeng.container.slot.AppEngSlot;
import appeng.helpers.IInterfaceHost;
import co.neeve.nae2.common.interfaces.IPatternMultiplierHost;
import co.neeve.nae2.common.slots.SlotPatternMultiplier;
import co.neeve.nae2.items.patternmultiplier.ObjPatternMultiplier;
import co.neeve.nae2.items.patternmultiplier.ToolPatternMultiplier;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ContainerInterface.class)
public class MixinContainerInterface extends MixinContainerUpgradeable implements IPatternMultiplierHost {
    private ObjPatternMultiplier pmtInventory;
    private List<AppEngSlot> patternMultiplierSlots = null;

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lappeng/helpers/IInterfaceHost;)V")
    public void ctor(InventoryPlayer ip, IInterfaceHost te, CallbackInfo cb) {
        World w = null;
        TileEntity mk;
        if (te instanceof TileEntity) {
            mk = (TileEntity) te;
            w = mk.getWorld();
        }

        if (te instanceof IPart) {
            mk = te.getTile();
            w = mk.getWorld();
        }

        int v;
        for (v = 0; v < ((IInventory) ip).getSizeInventory(); ++v) {
            ItemStack pii = ((IInventory) ip).getStackInSlot(v);
            if (!pii.isEmpty() && pii.getItem() instanceof ToolPatternMultiplier) {
                this.lockPlayerInventorySlot(v);
                this.pmtInventory = (ObjPatternMultiplier) ((IGuiItem) pii.getItem()).getGuiObject(pii, w, null);
                break;
            }
        }

        if (this.pmtInventory != null) {
            this.patternMultiplierSlots = new ArrayList<>();
            int offsetX = -63 - 18;
            int offsetY = 43 + 16 + 2;
            for (v = 0; v < 9; v++) {
                for (int u = 0; u < 4; u++) {
                    Slot slotPatternMultiplier = (new SlotPatternMultiplier(this.pmtInventory.getPatternInventory(),
                            this, v + u * 9, offsetX + u * 18, offsetY + v * 18, u, this.getInventoryPlayer())).setPlayerSide();
                    this.addSlotToContainer(slotPatternMultiplier);
                    this.patternMultiplierSlots.add((AppEngSlot) slotPatternMultiplier);
                }
            }
        }
    }

    @Override
    public ObjPatternMultiplier getPMTObject() {
        return this.pmtInventory;
    }

    @Override
    public List<AppEngSlot> getPatternMultiplierSlots() {
        return this.patternMultiplierSlots;
    }
}
