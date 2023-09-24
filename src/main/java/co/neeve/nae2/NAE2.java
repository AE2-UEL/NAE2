package co.neeve.nae2;

import co.neeve.nae2.client.gui.PatternMultiToolButtonHandler;
import co.neeve.nae2.client.models.ModelManager;
import co.neeve.nae2.common.RegistryHandler;
import co.neeve.nae2.common.items.patternmultitool.GuiHandlerPatternMultiTool;
import co.neeve.nae2.common.items.patternmultitool.net.HandlerPatternMultiTool;
import co.neeve.nae2.common.items.patternmultitool.net.PatternMultiToolPacket;
import co.neeve.nae2.server.WorldListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]",
	dependencies = "required-after:appliedenergistics2;required-after:mixinbooter")
public class NAE2 {
	public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);
	public static NAE2 instance;
	private final RegistryHandler registryHandler = new RegistryHandler();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		NAE2.instance = this;
		MinecraftForge.EVENT_BUS.register(new WorldListener());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerPatternMultiTool());

		NAE2.network.registerMessage(HandlerPatternMultiTool.class, PatternMultiToolPacket.class, 0, Side.SERVER);

		if (event.getSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new PatternMultiToolButtonHandler());
			MinecraftForge.EVENT_BUS.register(new ModelManager());
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		registryHandler.postInit();
	}
}