package de.crafty.eiv.recipe.cache;

import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.api.recipe.ModRecipeType;
import de.crafty.eiv.recipe.ClientRecipeCache;
import de.crafty.eiv.recipe.ClientRecipeManager;
import de.crafty.eiv.recipe.ServerRecipeManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ModRecipeCache {

    public static final ModRecipeCache INSTANCE = new ModRecipeCache();

    private static final Logger LOGGER = LoggerFactory.getLogger("ClientRecipeManager - ModRecipeCache");


    private ModCacheData cachingData;
    private final List<ModCacheData> receivedData;
    private int expectedTypes;

    private ModRecipeCache() {
        this.cachingData = ModCacheData.EMPTY;
        this.receivedData = new ArrayList<>();
    }

    public void clear() {
        this.cachingData = ModCacheData.EMPTY;
        this.receivedData.clear();
        this.expectedTypes = 0;
    }

    public boolean processRecipes() {
        boolean success = this.receivedData.size() == this.expectedTypes;

        for (int i = 0; i < this.receivedData.size(); i++) {
            ModCacheData data = this.receivedData.get(i);
            ClientRecipeManager.INSTANCE.status().setStatusStep("Processing Recipes (" + data.type().getId() + ")");
            ClientRecipeCache.INSTANCE.sortModType(data.type());
            ClientRecipeManager.INSTANCE.status().setStatusProgress(Math.round(((i + 1.0F) / this.receivedData.size()) * 100.0F) + "%");
        }
        this.receivedData.clear();
        return success;
    }


    public void modCacheStartReceived(int expectedTypes) {
        this.expectedTypes = expectedTypes;
    }

    public void startModCaching(ModRecipeType<?> type, int amount) {
        if (this.cachingData != ModCacheData.EMPTY) {
            LOGGER.error("Received new update while caching, skipping request...");
            return;
        }

        if (ModRecipeType.idFromType(type) == null) {
            LOGGER.error("Received unknown recipe type: {}", type);
        }

        LOGGER.info("Received recipe update for type: {}, caching {} Recipes...", type.getId(), amount);

        this.cachingData = new ModCacheData(type, amount, new ArrayList<>());

        ClientRecipeManager.INSTANCE.status().setStatusStep("Caching Recipes (" + type.getId() + ")");
        ClientRecipeManager.INSTANCE.status().setStatusProgress(0 + "/" + amount);
    }

    public void cacheModRecipe(ServerRecipeManager.ModRecipeEntry entry) {
        if (this.cachingData == ModCacheData.EMPTY) {
            LOGGER.error("Received recipe while idling, skipping request...");
            return;
        }

        if (this.cachingData.type() != entry.recipe().getRecipeType()) {
            LOGGER.error("Received recipe for type: {} while caching type: {}", entry.recipe().getRecipeType().getId(), this.cachingData.type().getId());
            return;
        }

        this.cachingData.received().add(entry);
        ClientRecipeManager.INSTANCE.status().setStatusProgress(this.cachingData.received().size() + "/" + this.cachingData.expectedAmount());
    }

    public void endModCaching(ModRecipeType<?> type) {
        if (this.cachingData == ModCacheData.EMPTY) {
            LOGGER.error("Received end-packet while idling => bad request");
            return;
        }

        if (this.cachingData.type() != type) {
            LOGGER.error("Received caching-end packet for type: {} while caching type: {} => ???", type, this.cachingData.type().getId());
            return;
        }

        if (this.cachingData.finishedSuccessfully()) {
            ModCacheData cachedCache = this.cachingData;
            this.receivedData.add(cachedCache);

            this.cachingData = ModCacheData.EMPTY;
            LOGGER.info("Successfully updated recipes for type: {}", cachedCache.type().getId());
            ClientRecipeCache.INSTANCE.updateModType(cachedCache.type(), cachedCache.received());
        } else {
            this.cachingData = ModCacheData.EMPTY;
            LOGGER.error("Expected amount of recipes does not match the amount of recipes received => Update failed");
        }

    }


    record ModCacheData(ModRecipeType<?> type, int expectedAmount, List<ServerRecipeManager.ModRecipeEntry> received) {

        static final ModCacheData EMPTY = null;

        boolean finishedSuccessfully() {
            return this.received.size() == this.expectedAmount;
        }
    }
}
