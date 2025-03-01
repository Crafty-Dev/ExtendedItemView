package de.crafty.eiv;

import de.crafty.eiv.api.recipe.ItemViewRecipes;
import de.crafty.eiv.extra.FluidItemModel;
import de.crafty.eiv.network.EivNetworkClient;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.recipe.item.FluidItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;

public class ExtendedItemViewClient implements ClientModInitializer {


    public static final KeyMapping USAGE_KEYBIND = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.eiv.usage", 85, "key.categories.eiv")
    );

    public static final KeyMapping RECIPE_KEYBIND = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.eiv.recipe", 82, "key.categories.eiv")
    );

    public static final MenuType<RecipeViewMenu> RECIPE_VIEW_MENU = Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "recipe_view"), new MenuType<>(RecipeViewMenu::new, FeatureFlagSet.of()));

    public static final ModelLayerLocation FLUID_ITEM_MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "fluiditem"), "inventory");

    @Override
    public void onInitializeClient() {

        MenuScreens.register(RECIPE_VIEW_MENU, RecipeViewScreen::new);
        EivNetworkClient.registerClientHandlers();


        EntityModelLayerRegistry.registerModelLayer(FLUID_ITEM_MODEL_LAYER, FluidItemModel::createFluidLayer);
    }



}
