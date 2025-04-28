package de.crafty.eiv.network;

import de.crafty.eiv.network.payload.ClientboundAllUpdatesFinishedPayload;
import de.crafty.eiv.network.payload.ClientboundGeneralUpdateStartedPayload;
import de.crafty.eiv.network.payload.mod.ClientboundModRecipeUpdatePayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdateEndPayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdatePayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdateStartPayload;
import de.crafty.eiv.network.payload.transfer.ClientboundUpdateTransferCachePayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdateEndPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdatePayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdateStartPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeRecipeUpdatePayload;
import de.crafty.eiv.recipe.ClientRecipeManager;
import de.crafty.eiv.recipe.cache.ModRecipeCache;
import de.crafty.eiv.recipe.cache.VanillaRecipeCache;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class EivNetworkClient {


    public static void registerClientHandlers() {

        registerVanillaHandlers();
        registerModHandlers();

        ClientPlayNetworking.registerGlobalReceiver(ClientboundGeneralUpdateStartedPayload.TYPE, (payload, context) -> {
            context.client().execute(ClientRecipeManager.INSTANCE::startUpdate);
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundAllUpdatesFinishedPayload.TYPE, (payload, context) -> {
           context.client().execute(ClientRecipeManager.INSTANCE::processRecipes);
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundUpdateTransferCachePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if(context.client().screen instanceof RecipeViewScreen viewScreen)
                    viewScreen.getMenu().updateTransferCache();
            });
        });

    }

    private static void registerVanillaHandlers() {

        ClientPlayNetworking.registerGlobalReceiver(ClientboundVanillaLikeRecipeUpdatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                VanillaRecipeCache.INSTANCE.vanillaCacheStartReceived(payload.types());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundVanillaLikeTypeUpdateStartPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                VanillaRecipeCache.INSTANCE.startVanillaCaching(payload.recipeType(), payload.recipeAmount());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundVanillaLikeTypeUpdatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                VanillaRecipeCache.INSTANCE.cacheVanillaLikeRecipe(payload.recipe());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundVanillaLikeTypeUpdateEndPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                VanillaRecipeCache.INSTANCE.endVanillaCaching(payload.recipeType());
            });
        });

    }

    private static void registerModHandlers() {

        ClientPlayNetworking.registerGlobalReceiver(ClientboundModRecipeUpdatePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ModRecipeCache.INSTANCE.modCacheStartReceived(payload.types());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundModTypeUpdateStartPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
               ModRecipeCache.INSTANCE.startModCaching(payload.recipeType(), payload.amount());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundModTypeUpdatePayload.TYPE, (payload, context) -> {
           context.client().execute(() -> {
               ModRecipeCache.INSTANCE.cacheModRecipe(payload.entry());
           });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundModTypeUpdateEndPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
               ModRecipeCache.INSTANCE.endModCaching(payload.recipeType());
            });
        });

    }
}
