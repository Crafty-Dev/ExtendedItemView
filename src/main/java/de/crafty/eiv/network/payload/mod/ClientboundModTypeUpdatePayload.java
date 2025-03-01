package de.crafty.eiv.network.payload.mod;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.recipe.ServerRecipeManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundModTypeUpdatePayload(ServerRecipeManager.ModRecipeEntry entry) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundModTypeUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            ServerRecipeManager.ModRecipeEntry.STREAM_CODEC,
            ClientboundModTypeUpdatePayload::entry,
            ClientboundModTypeUpdatePayload::new
    );

    public static final Type<ClientboundModTypeUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "mot_update"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
