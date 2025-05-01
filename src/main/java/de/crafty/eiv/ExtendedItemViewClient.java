package de.crafty.eiv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.crafty.eiv.extra.FluidItemModel;
import de.crafty.eiv.network.EivNetworkClient;
import de.crafty.eiv.overlay.ItemBookmarkOverlay;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExtendedItemViewClient implements ClientModInitializer {


    public static final KeyMapping USAGE_KEYBIND = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.eiv.usage", 85, "key.categories.eiv")
    );

    public static final KeyMapping RECIPE_KEYBIND = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.eiv.recipe", 82, "key.categories.eiv")
    );

    public static final KeyMapping TOGGLE_OVERLAY_KEYBIND = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.eiv.toggle_overlay", 79, "key.categories.eiv")
    );

    public static final KeyMapping ADD_BOOKMARK_KEYBIND = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.eiv.bookmark", 65, "key.categories.eiv")
    );

    private static final List<KeyMapping> EIV_MAPPINGS = List.of(USAGE_KEYBIND, RECIPE_KEYBIND, TOGGLE_OVERLAY_KEYBIND, ADD_BOOKMARK_KEYBIND);

    public static final MenuType<RecipeViewMenu> RECIPE_VIEW_MENU = Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "recipe_view"), new MenuType<>(RecipeViewMenu::new, FeatureFlagSet.of()));

    public static final ModelLayerLocation FLUID_ITEM_MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "fluiditem"), "inventory");


    private static ExtendedItemViewClient instance;

    @Override
    public void onInitializeClient() {
        instance = this;

        MenuScreens.register(RECIPE_VIEW_MENU, RecipeViewScreen::new);
        EivNetworkClient.registerClientHandlers();


        EntityModelLayerRegistry.registerModelLayer(FLUID_ITEM_MODEL_LAYER, FluidItemModel::createFluidLayer);


        //Save bookmarks
        File eivFolder = new File("eiv");
        if (eivFolder.mkdirs())
            ExtendedItemView.LOGGER.info("EIV folder not present, creating...");

        File bookmarks = new File("eiv/bookmarks.json");
        if (bookmarks.exists()) {
            try {
                JsonObject contentJson = JsonParser.parseString(FileUtils.readFileToString(bookmarks, StandardCharsets.UTF_8)).getAsJsonObject();
                System.out.println(contentJson);
                ItemBookmarkOverlay.INSTANCE.loadBookmarkedItems(contentJson);
            } catch (Exception e) {
                ExtendedItemView.LOGGER.error("Failed to load bookmarks from file, skipping...", e);
            }
        }
    }

    public static ExtendedItemViewClient getInstance() {
        return instance;
    }

    public void onExit() {

        JsonObject encoded = new JsonObject();
        ItemBookmarkOverlay.INSTANCE.saveBookmarkedItems(encoded);

        File bookmarkFile = new File("eiv/bookmarks.json");

        try {
            if (!bookmarkFile.exists())
                bookmarkFile.createNewFile();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileUtils.writeStringToFile(bookmarkFile, gson.toJson(encoded));
        } catch (Exception e) {
            ExtendedItemView.LOGGER.error("Failed to save bookmarks to file", e);
        }


    }

    public static void excludeEivMappings() {
        KeyMapping.MAP.clear();

        for (KeyMapping keyMapping : KeyMapping.ALL.values()) {
            if (KeyMapping.MAP.containsKey(keyMapping.key) && EIV_MAPPINGS.contains(keyMapping)) {
                continue;
            }

            KeyMapping.MAP.put(keyMapping.key, keyMapping);
        }
    }
}
