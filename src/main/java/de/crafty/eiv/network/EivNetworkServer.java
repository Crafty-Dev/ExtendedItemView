package de.crafty.eiv.network;

import de.crafty.eiv.network.payload.ServerboundRequestRecipesPayload;
import de.crafty.eiv.recipe.ServerRecipeManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class EivNetworkServer {

    public static void registerServerHandlers() {

        ServerPlayNetworking.registerGlobalReceiver(ServerboundRequestRecipesPayload.TYPE, (payload, context) -> {
           context.server().execute(() -> {
               ServerRecipeManager.INSTANCE.informAboutAllRecipes(context.player());
           });
        });

    }

}
