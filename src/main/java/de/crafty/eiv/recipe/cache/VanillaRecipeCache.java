package de.crafty.eiv.recipe.cache;

import de.crafty.eiv.recipe.ClientRecipeCache;
import de.crafty.eiv.recipe.ClientRecipeManager;
import de.crafty.eiv.recipe.ServerRecipeManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class VanillaRecipeCache {

    public static final VanillaRecipeCache INSTANCE = new VanillaRecipeCache();

    private static final Logger LOGGER = LoggerFactory.getLogger("ClientRecipeManager - VanillaRecipeCache");


    private VanillaCacheData cachingData;
    private final List<VanillaCacheData> receivedData;
    private int expectedTypes;

    private VanillaRecipeCache() {
        this.cachingData = VanillaCacheData.EMPTY;
        this.receivedData = new ArrayList<>();
    }

    public void clear(){
        this.cachingData = VanillaCacheData.EMPTY;
        this.receivedData.clear();
        this.expectedTypes = 0;
    }


    public boolean processRecipes(){
        boolean success = this.receivedData.size() == this.expectedTypes;

        for (int i = 0; i < this.receivedData.size(); i++) {
            VanillaCacheData data = this.receivedData.get(i);
            ClientRecipeManager.INSTANCE.status().setStatusStep("Processing Recipes (" + this.getPrintableRecipeType(data.recipeType()) + ")");
            ClientRecipeCache.INSTANCE.sortVanillaLikeType(data.recipeType());
            ClientRecipeManager.INSTANCE.status().setStatusProgress(Math.round(((i + 1.0F) / this.receivedData.size()) * 100.0F) + "%");
        }
        this.receivedData.clear();
        return success;
    }

    public void vanillaCacheStartReceived(int expectedTypes) {
        this.expectedTypes = expectedTypes;
    }


    public void startVanillaCaching(RecipeType<?> recipeType, int amount) {
        if (this.cachingData != VanillaCacheData.EMPTY) {
            LOGGER.error("Received new update while caching, skipping request...");
            return;
        }

        if (BuiltInRegistries.RECIPE_TYPE.getKey(recipeType) == null) {
            LOGGER.error("Received unknown recipe type: {}", recipeType);
        }

        LOGGER.info("Received recipe update for type: {}, caching {} Recipes...", BuiltInRegistries.RECIPE_TYPE.getKey(recipeType), amount);

        this.cachingData = new VanillaCacheData(recipeType, amount, new ArrayList<>());

        ClientRecipeManager.INSTANCE.status().setStatusStep("Caching Recipes (" + this.getPrintableRecipeType(recipeType) + ")");
        ClientRecipeManager.INSTANCE.status().setStatusProgress(0 + "/" + amount);

    }

    public void cacheVanillaLikeRecipe(ServerRecipeManager.VanillaRecipeEntry entry) {
        if (this.cachingData == VanillaCacheData.EMPTY) {
            LOGGER.error("Received recipe while idling, skipping request...");
            return;
        }

        if (this.cachingData.recipeType() != entry.recipe().getType()) {
            LOGGER.error("Received recipe for type: {} while caching type: {}", this.getPrintableRecipeType(entry.recipe().getType()), this.getPrintableRecipeType(this.cachingData.recipeType()));
            return;
        }

        this.cachingData.received().add(entry);
        ClientRecipeManager.INSTANCE.status().setStatusProgress(this.cachingData.received().size() + "/" + this.cachingData.expectedAmount());
    }

    public void endVanillaCaching(RecipeType<?> recipeType) {
        if (this.cachingData == VanillaCacheData.EMPTY) {
            LOGGER.error("Received end-packet while idling => bad request");
            return;
        }

        if (this.cachingData.recipeType() != recipeType) {
            LOGGER.error("Received caching-end packet for type: {} while caching type: {} => ???", recipeType, BuiltInRegistries.RECIPE_TYPE.getKey(this.cachingData.recipeType()));
            return;
        }

        if (this.cachingData.finishedSuccessfully()) {
            VanillaCacheData cachedCache = this.cachingData;
            this.receivedData.add(cachedCache);

            this.cachingData = VanillaCacheData.EMPTY;
            LOGGER.info("Successfully updated recipes for type: {}", this.getPrintableRecipeType(cachedCache.recipeType()));
            ClientRecipeCache.INSTANCE.updateVanillaLikeType(cachedCache.recipeType(), cachedCache.received());
        } else {
            this.cachingData = VanillaCacheData.EMPTY;
            LOGGER.error("Expected amount of recipes does not match the amount of recipes received => Update failed");
        }
    }

    String getPrintableRecipeType(RecipeType<?> recipeType) {
        ResourceLocation key = BuiltInRegistries.RECIPE_TYPE.getKey(recipeType);
        return key == null ? recipeType.toString() : key.toString();
    }

    record VanillaCacheData(RecipeType<?> recipeType, int expectedAmount, List<ServerRecipeManager.VanillaRecipeEntry> received) {

        static final VanillaCacheData EMPTY = null;


        boolean finishedSuccessfully() {
            return this.received.size() == this.expectedAmount;
        }

    }
}
