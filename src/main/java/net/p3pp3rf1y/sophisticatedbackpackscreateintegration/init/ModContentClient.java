package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.client.KeybindHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSubBackpackOpenMessage;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client.MountedBackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client.MountedBackpackSettingsScreen;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageInventorySlot;
import net.p3pp3rf1y.sophisticatedcore.compat.trashslot.TrashSlotScreenRegistry;

public class ModContentClient {
	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ModContentClient::onMenuScreenRegister);

		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(EventPriority.HIGH, ModContentClient::handleGuiMouseKeyPress);
		eventBus.addListener(EventPriority.HIGH, ModContentClient::handleGuiKeyPress);
	}

	private static void onMenuScreenRegister(RegisterEvent event) {
		if (!event.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES)) {
			return;
		}

		MenuScreens.register(ModContent.MOUNTED_BACKPACK_CONTAINER_TYPE.get(), MountedBackpackScreen::constructScreen);
		MenuScreens.register(ModContent.MOUNTED_BACKPACK_SETTINGS_CONTAINER_TYPE.get(), MountedBackpackSettingsScreen::constructScreen);

		TrashSlotScreenRegistry.registerScreen(MountedBackpackScreen.class);
	}

	public static void handleGuiKeyPress(ScreenEvent.KeyPressed.Pre event) {
		InputConstants.Key key = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
		if (KeybindHandler.SORT_KEYBIND.isActiveAndMatches(key) && tryCallSort(event.getScreen()) || KeybindHandler.BACKPACK_OPEN_KEYBIND.isActiveAndMatches(key) && sendBackpackOpenOrCloseMessage()) {
			event.setCanceled(true);
		}
	}

	public static void handleGuiMouseKeyPress(ScreenEvent.MouseButtonPressed.Pre event) {
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(event.getButton());
		if (KeybindHandler.SORT_KEYBIND.isActiveAndMatches(input) && tryCallSort(event.getScreen()) || KeybindHandler.BACKPACK_OPEN_KEYBIND.isActiveAndMatches(input) && sendBackpackOpenOrCloseMessage()) {
			event.setCanceled(true);
		}
	}

	private static boolean tryCallSort(Screen gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.player.containerMenu instanceof MountedBackpackContainerMenu container && gui instanceof MountedBackpackScreen screen) {
			MouseHandler mh = mc.mouseHandler;
			double mouseX = mh.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
			double mouseY = mh.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
			Slot selectedSlot = screen.findSlot(mouseX, mouseY);
			if (selectedSlot == null || container.isNotPlayersInventorySlot(selectedSlot.index)) {
				container.sort();
				return true;
			}
		}
		return false;
	}

	private static boolean sendBackpackOpenOrCloseMessage() {
		Screen screen = Minecraft.getInstance().screen;
		if (screen instanceof MountedBackpackScreen mountedBackpackScreen) {
			Slot slot = mountedBackpackScreen.getSlotUnderMouse();

			if (mountedBackpackScreen.getMenu().isFirstLevelStorage() && slot instanceof StorageInventorySlot && slot.getItem().getItem() instanceof BackpackItem && slot.getItem().getCount() == 1) {
				mountedBackpackScreen.getMenu().getContext().getSubBackpackContext(slot.index);
				SBPPacketHandler.INSTANCE.sendToServer(new MountedSubBackpackOpenMessage(slot.index));
				return true;
			}
		}
		return false;
	}
}
