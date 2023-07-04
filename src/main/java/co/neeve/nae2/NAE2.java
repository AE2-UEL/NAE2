package co.neeve.nae2;

import co.neeve.nae2.item.patternmultiplier.GuiHandlerPatternMultiplier;
import co.neeve.nae2.item.patternmultiplier.ItemPatternMultiplier;
import co.neeve.nae2.item.patternmultiplier.net.HandlerPatternMultiplier;
import co.neeve.nae2.item.patternmultiplier.net.PatternMultiplierPacket;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:appliedenergistics2")
public class NAE2 {
    public static NAE2 instance;

    @GameRegistry.ObjectHolder(Tags.MODID + ":pattern_multiplier")
    public static ItemPatternMultiplier itemPatternMultiplier = new ItemPatternMultiplier();

    public static SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NAE2.instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerPatternMultiplier());

        NAE2.network.registerMessage(HandlerPatternMultiplier.class, PatternMultiplierPacket.class, 0, Side.SERVER);

    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(itemPatternMultiplier);
    }
}