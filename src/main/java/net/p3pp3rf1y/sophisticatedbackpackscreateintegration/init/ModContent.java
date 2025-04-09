package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.SophisticatedBackpacksCreateIntegration;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSophisticatedBackpackType;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSubBackpackOpenMessage;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryMessage;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.SophisticatedBackpackMovementBehaviour;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.schematic.SophisticatedBackpackSafeNbtWriter;

import java.util.function.Supplier;

public class ModContent {
	private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(SophisticatedBackpacksCreateIntegration.MOD_ID)
			.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
			.setTooltipModifierFactory(item ->
					new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
							.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
			);

	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SophisticatedBackpacksCreateIntegration.MOD_ID);

	public static final RegistryEntry<MountedSophisticatedBackpackType> SOPHISTICATED_MOUNTED_BACKPACK_TYPE = REGISTRATE.mountedItemStorage("sophisticated_backpack", MountedSophisticatedBackpackType::new).register();

	public static final Supplier<MenuType<MountedBackpackContainerMenu>> MOUNTED_BACKPACK_CONTAINER_TYPE = MENU_TYPES.register("mounted_backpack",
			() -> IForgeMenuType.create(MountedBackpackContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedBackpackSettingsContainerMenu>> MOUNTED_BACKPACK_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("mounted_backpack_settings",
			() -> IForgeMenuType.create(MountedBackpackSettingsContainerMenu::fromBuffer));

	public static void registerHandler(IEventBus modBus) {
		REGISTRATE.registerEventListeners(modBus);
		MENU_TYPES.register(modBus);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			ModContentClient.registerHandlers(modBus);
		}

		modBus.addListener(ModContent::onModSetup);
	}

	private static void onModSetup(FMLCommonSetupEvent event) {
		BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof BackpackBlock)
				.forEach(block -> {
					MountedItemStorageType.REGISTRY.register(block, SOPHISTICATED_MOUNTED_BACKPACK_TYPE.get());
					MovementBehaviour.REGISTRY.register(block, SophisticatedBackpackMovementBehaviour.INSTANCE);
				});
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.BACKPACK_TILE_TYPE.get(), SophisticatedBackpackSafeNbtWriter.INSTANCE);
		event.enqueueWork(() -> {
			SBPPacketHandler.INSTANCE.registerMessage(OpenMountedBackpackInventoryMessage.class, OpenMountedBackpackInventoryMessage::encode, OpenMountedBackpackInventoryMessage::decode, OpenMountedBackpackInventoryMessage::onMessage);
			SBPPacketHandler.INSTANCE.registerMessage(MountedSubBackpackOpenMessage.class, MountedSubBackpackOpenMessage::encode, MountedSubBackpackOpenMessage::decode, MountedSubBackpackOpenMessage::onMessage);
		});
	}
}
