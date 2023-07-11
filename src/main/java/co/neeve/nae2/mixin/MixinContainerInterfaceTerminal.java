package co.neeve.nae2.mixin;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.slot.AppEngSlot;
import appeng.helpers.InventoryAction;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.util.helpers.ItemHandlerUtil;
import co.neeve.nae2.common.enums.PatternMultiplierInventories;
import co.neeve.nae2.common.interfaces.IPatternMultiplierHost;
import co.neeve.nae2.common.slots.SlotPatternMultiplier;
import co.neeve.nae2.items.patternmultiplier.ObjPatternMultiplier;
import co.neeve.nae2.items.patternmultiplier.ToolPatternMultiplier;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ContainerInterfaceTerminal.class)
public class MixinContainerInterfaceTerminal extends AEBaseContainer implements IPatternMultiplierHost {
    private ObjPatternMultiplier pmtInventory;
    private List<AppEngSlot> patternMultiplierSlots = null;

    public MixinContainerInterfaceTerminal(InventoryPlayer ip, TileEntity myTile, IPart myPart) {
        super(ip, myTile, myPart);
    }

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lappeng/parts/reporting/PartInterfaceTerminal;)V")
    public void ctor(InventoryPlayer ip, PartInterfaceTerminal anchor, CallbackInfo cb) {
        int v;

        World w = null;
        TileEntity mk;
        if (anchor != null) {
            mk = anchor.getTile();
            w = mk.getWorld();
        }

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
            int offsetY = 43 + 16;

            for (v = 0; v < 9; v++) {
                for (int u = 0; u < 4; u++) {
                    Slot slotPatternMultiplier = (new SlotPatternMultiplier(this.pmtInventory.getPatternInventory(PatternMultiplierInventories.PMT),
                            this, v + u * 9, offsetX + u * 18, offsetY + v * 18, u, this.getInventoryPlayer())).setPlayerSide();
                    this.addSlotToContainer(slotPatternMultiplier);
                    this.patternMultiplierSlots.add((AppEngSlot) slotPatternMultiplier);
                }
            }
        }
    }

    @Inject(method = "Lappeng/container/implementations/ContainerInterfaceTerminal;doAction(Lnet/minecraft/entity/player/EntityPlayerMP;Lappeng/helpers/InventoryAction;IJ)V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lappeng/util/InventoryAdaptor;getAdaptor(Lnet/minecraft/entity/player/EntityPlayer;)Lappeng/util/InventoryAdaptor;",
                    ordinal = 0
            ),
            remap = false
    )
    public void injectPMTInventory(EntityPlayerMP player, InventoryAction action, int slot, long id, CallbackInfo ci, @Local(ordinal = 0) IItemHandler theSlot) {
        if (this.pmtInventory != null) {
            IItemHandler pmtPatterns = this.pmtInventory.getPatternInventory(PatternMultiplierInventories.PMT);
            List<AppEngSlot> pmtSlots = this.getPatternMultiplierSlots();
            ItemStack partialIs = theSlot.getStackInSlot(0);

            for (int i = 0; i < pmtPatterns.getSlots(); i++) {
                if (!pmtSlots.get(i).isItemValid(partialIs)) continue;

                partialIs = pmtPatterns.insertItem(i, partialIs, false);
                if (partialIs.isEmpty()) {
                    break;
                }
            }
            ItemHandlerUtil.setStackInSlot(theSlot, 0, partialIs);
        }
    }

    @Override
    public List<AppEngSlot> getPatternMultiplierSlots() {
        return patternMultiplierSlots;
    }

    @Override
    public ObjPatternMultiplier getPMTObject() {
        return this.pmtInventory;
    }
}
