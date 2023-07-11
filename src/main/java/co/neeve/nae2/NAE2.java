package co.neeve.nae2;

import co.neeve.nae2.common.Items;
import co.neeve.nae2.items.patternmultiplier.GuiHandlerPatternMultiplier;
import co.neeve.nae2.items.patternmultiplier.net.HandlerPatternMultiplier;
import co.neeve.nae2.items.patternmultiplier.net.PatternMultiplierPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:appliedenergistics2")
public class NAE2 {
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);
    public static NAE2 instance;
    private Items itemHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NAE2.instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        itemHandler = new Items();
        MinecraftForge.EVENT_BUS.register(itemHandler);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerPatternMultiplier());

        NAE2.network.registerMessage(HandlerPatternMultiplier.class, PatternMultiplierPacket.class, 0, Side.SERVER);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        itemHandler.init();
    }
}