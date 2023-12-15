package co.neeve.nae2;

import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.net.NetHandler;
import co.neeve.nae2.common.registration.Registration;
import co.neeve.nae2.common.sync.GuiHandler;
import co.neeve.nae2.server.WorldListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]",
	dependencies = "required-after:appliedenergistics2;required-after:mixinbooter@[8.3,)")
public class NAE2 {
	public static NAE2 instance;
	private static ConfigManager configManager;
	private final Logger logger = LogManager.getLogger("NAE2");
	private final NetHandler network = new NetHandler();
	private Registration registration;

	private GuiHandler guiHandler;

	@SideOnly(Side.CLIENT)
	private ItemStack icon;

	public static void setupConfig() {
		if (configManager == null) {
			configManager = new ConfigManager();
		}
	}

	public static SimpleNetworkWrapper net() {
		return instance.network.getChannel();
	}

	public static GuiHandler gui() {
		return instance.guiHandler;
	}

	public static Registration definitions() {
		return instance.registration;
	}

	@SideOnly(Side.CLIENT)
	public static ItemStack icon() {
		return instance.icon;
	}

	public static Logger logger() {
		return instance.logger;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		this.registration = new Registration();

		setupConfig();

		NAE2.instance = this;
		MinecraftForge.EVENT_BUS.register(new WorldListener());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, this.guiHandler = new GuiHandler());

		this.registration.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		this.registration.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (event.getSide() == Side.CLIENT) {
			this.icon = NAE2.definitions().items().patternMultiTool().maybeStack(1).orElse(ItemStack.EMPTY);
		}

		this.registration.postInit(event);
	}

	public static class ConfigManager {

		public ConfigManager() {
			var file = new File("config/" + Tags.MODID + ".cfg");
			var config = new Configuration(file);

			for (var feature : Features.values()) {
				var lowerCase = feature.name().toLowerCase();

				var featureCategory = config.getCategory(lowerCase);
				var entry = featureCategory.computeIfAbsent("enabled", x -> new Property("enabled", "true",
					Property.Type.BOOLEAN));

				feature.setEnabled(entry.getBoolean(true));

				var subFeatures = feature.getSubFeatures();
				if (subFeatures != null) {
					for (var subFeature : subFeatures) {
						var subFeatureLowerCase = subFeature.name().toLowerCase();
						var subFeatureEntry = featureCategory.computeIfAbsent(subFeatureLowerCase,
							x -> new Property(subFeatureLowerCase, "true", Property.Type.BOOLEAN));

						subFeature.setEnabled(feature.isEnabled() && subFeatureEntry.getBoolean(true));
						subFeatureEntry.setComment(subFeature.getDescription());
					}
				}
			}

			config.save();
		}
	}
}