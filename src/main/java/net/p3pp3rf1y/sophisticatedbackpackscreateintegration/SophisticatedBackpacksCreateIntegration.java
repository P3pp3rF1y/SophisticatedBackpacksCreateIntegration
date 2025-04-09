package net.p3pp3rf1y.sophisticatedbackpackscreateintegration;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedBackpacksCreateIntegration.MOD_ID)
public class SophisticatedBackpacksCreateIntegration {
	public static final String MOD_ID = "sophisticatedbackpackscreateintegration";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public SophisticatedBackpacksCreateIntegration() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModContent.registerHandler(modBus);
	}

	public static ResourceLocation getRL(String regName) {
		return new ResourceLocation(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}
}
