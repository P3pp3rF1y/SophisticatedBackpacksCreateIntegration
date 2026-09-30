package net.p3pp3rf1y.sophisticatedbackpackscreateintegration;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedBackpacksCreateIntegration.MOD_ID)
public class SophisticatedBackpacksCreateIntegration {
	public static final String MOD_ID = "sophisticatedbackpackscreateintegration";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static String networkProtocolVersion;

	@SuppressWarnings("java:S1118") // needs to be public for mod to work
	public SophisticatedBackpacksCreateIntegration(IEventBus modBus, Dist dist, ModContainer container) {
		networkProtocolVersion = container.getModInfo().getVersion().toString();
		// Create integration is dormant until a 26.3 Create build is available.
	}

	public static Identifier getIdentifier(String regName) {
		return Identifier.parse(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}

	public static String getNetworkProtocolVersion() {
		return networkProtocolVersion;
	}
}
