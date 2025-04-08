package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.SophisticatedBackpacksCreateIntegration;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSophisticatedBackpackType;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSubBackpackOpenPayload;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryPayload;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.SophisticatedBackpackMovementBehaviour;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.schematic.SophisticatedBackpackSafeNbtWriter;

import java.util.function.Supplier;

public class ModContent {
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, SophisticatedBackpacksCreateIntegration.MOD_ID);

	public static final DeferredRegister<MountedItemStorageType<?>> ITEM_STORAGE_TYPES = DeferredRegister.create(CreateBuiltInRegistries.MOUNTED_ITEM_STORAGE_TYPE, SophisticatedBackpacks.MOD_ID);

	public static final DeferredHolder<MountedItemStorageType<?>, MountedSophisticatedBackpackType> SOPHISTICATED_MOUNTED_BACKPACK_TYPE = ITEM_STORAGE_TYPES.register("sophisticated_backpack", MountedSophisticatedBackpackType::new);

	public static final Supplier<MenuType<MountedBackpackContainerMenu>> MOUNTED_BACKPACK_CONTAINER_TYPE = MENU_TYPES.register("mounted_backpack",
			() -> IMenuTypeExtension.create(MountedBackpackContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MountedBackpackSettingsContainerMenu>> MOUNTED_BACKPACK_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("mounted_backpack_settings",
			() -> IMenuTypeExtension.create(MountedBackpackSettingsContainerMenu::fromBuffer));

	public static void registerHandler(IEventBus modBus) {
		ITEM_STORAGE_TYPES.register(modBus);
		MENU_TYPES.register(modBus);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			ModContentClient.registerHandlers(modBus);
		}

		modBus.addListener(ModContent::onModSetup);
		modBus.addListener(ModContent::registerPayloads);
	}

	private static void onModSetup(FMLCommonSetupEvent event) {
		BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof BackpackBlock)
				.forEach(block -> {
					MountedItemStorageType.REGISTRY.register(block, SOPHISTICATED_MOUNTED_BACKPACK_TYPE.get());
					MovementBehaviour.REGISTRY.register(block, SophisticatedBackpackMovementBehaviour.INSTANCE);
				});
		SafeNbtWriterRegistry.REGISTRY.register(ModBlocks.BACKPACK_TILE_TYPE.get(), SophisticatedBackpackSafeNbtWriter.INSTANCE);
	}

	private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(SophisticatedBackpacksCreateIntegration.MOD_ID).versioned("1.0");
		registrar.playToServer(OpenMountedBackpackInventoryPayload.TYPE, OpenMountedBackpackInventoryPayload.STREAM_CODEC, OpenMountedBackpackInventoryPayload::handlePayload);
		registrar.playToServer(MountedSubBackpackOpenPayload.TYPE, MountedSubBackpackOpenPayload.STREAM_CODEC, MountedSubBackpackOpenPayload::handlePayload);
	}
}
