package net.p3pp3rf1y.sophisticatedbackpackscreateintegration;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModCompat;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.network.BackpackCreatePacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedBackpacksCreateIntegration.MOD_ID)
public class SophisticatedBackpacksCreateIntegration {
	public static final String MOD_ID = "sophisticatedbackpackscreateintegration";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static String networkProtocolVersion;

	public SophisticatedBackpacksCreateIntegration() {
		networkProtocolVersion = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModContent.registerHandler(modBus);
		modBus.addListener(SophisticatedBackpacksCreateIntegration::setup);
		ModCompat.initCompats();
	}

	private static void setup(FMLCommonSetupEvent event) {
		BackpackCreatePacketHandler.INSTANCE.init();
		ModCompat.compatsSetup();
	}

	public static ResourceLocation getRL(String regName) {
		return new ResourceLocation(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}

	public static String getNetworkProtocolVersion() {
		return networkProtocolVersion;
	}
}
