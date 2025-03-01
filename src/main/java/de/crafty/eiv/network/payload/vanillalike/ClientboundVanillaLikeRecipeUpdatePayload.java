package de.crafty.eiv.network.payload.vanillalike;

import de.crafty.eiv.ExtendedItemView;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundVanillaLikeRecipeUpdatePayload(int types) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundVanillaLikeRecipeUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClientboundVanillaLikeRecipeUpdatePayload::types,
            ClientboundVanillaLikeRecipeUpdatePayload::new
    );

    public static final Type<ClientboundVanillaLikeRecipeUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "vl_update"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
