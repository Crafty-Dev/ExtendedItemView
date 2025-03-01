package de.crafty.eiv.network;

import de.crafty.eiv.network.payload.*;
import de.crafty.eiv.network.payload.mod.ClientboundModRecipeUpdatePayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdateEndPayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdatePayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdateStartPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdateEndPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdatePayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdateStartPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeRecipeUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class EivNetworkManager {


    public static void registerPayloads() {

        PayloadTypeRegistry.playC2S().register(ServerboundRequestRecipesPayload.TYPE, ServerboundRequestRecipesPayload.STREAM_CODEC);


        //Vanilla-like
        PayloadTypeRegistry.playS2C().register(ClientboundVanillaLikeRecipeUpdatePayload.TYPE, ClientboundVanillaLikeRecipeUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundVanillaLikeTypeUpdateStartPayload.TYPE, ClientboundVanillaLikeTypeUpdateStartPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundVanillaLikeTypeUpdatePayload.TYPE, ClientboundVanillaLikeTypeUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundVanillaLikeTypeUpdateEndPayload.TYPE, ClientboundVanillaLikeTypeUpdateEndPayload.STREAM_CODEC);


        //Mod
        PayloadTypeRegistry.playS2C().register(ClientboundModRecipeUpdatePayload.TYPE, ClientboundModRecipeUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundModTypeUpdateStartPayload.TYPE, ClientboundModTypeUpdateStartPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundModTypeUpdatePayload.TYPE, ClientboundModTypeUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundModTypeUpdateEndPayload.TYPE, ClientboundModTypeUpdateEndPayload.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(ClientboundGeneralUpdateStartedPayload.TYPE, ClientboundGeneralUpdateStartedPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundAllUpdatesFinishedPayload.TYPE, ClientboundAllUpdatesFinishedPayload.STREAM_CODEC);
    }

}
