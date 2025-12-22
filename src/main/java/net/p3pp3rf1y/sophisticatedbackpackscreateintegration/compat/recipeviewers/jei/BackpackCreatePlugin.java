package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.compat.recipeviewers.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.SophisticatedBackpacksCreateIntegration;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client.MountedBackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client.MountedBackpackSettingsScreen;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiCraftingContainerRecipeTransferHandlerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiSettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiStorageGhostIngredientHandler;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class BackpackCreatePlugin implements IModPlugin {

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(SophisticatedBackpacksCreateIntegration.MOD_ID, "default");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(MountedBackpackScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MountedBackpackScreen gui) {
				List<Rect2i> ret = new ArrayList<>();
				gui.getUpgradeSlotsRectangle().ifPresent(ret::add);
				ret.addAll(gui.getUpgradeSettingsControl().getTabRectangles());
				gui.getSortButtonsRectangle().ifPresent(ret::add);
				return ret;
			}
		});

		registration.addGuiContainerHandler(MountedBackpackSettingsScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MountedBackpackSettingsScreen gui) {
				return new ArrayList<>(gui.getExtendedControlsRectangles());
			}
		});

		registration.addGhostIngredientHandler(MountedBackpackScreen.class, new JeiStorageGhostIngredientHandler<>());
		registration.addGhostIngredientHandler(MountedBackpackSettingsScreen.class, new JeiSettingsGhostIngredientHandler<>());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
		IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
		registration.addRecipeTransferHandler(new JeiCraftingContainerRecipeTransferHandlerBase<MountedBackpackContainerMenu, RecipeHolder<CraftingRecipe>>(handlerHelper, stackHelper) {
			@Override
			public Class<MountedBackpackContainerMenu> getContainerClass() {
				return MountedBackpackContainerMenu.class;
			}

			@Override
			public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
				return RecipeTypes.CRAFTING;
			}
		}, RecipeTypes.CRAFTING);
	}
}
