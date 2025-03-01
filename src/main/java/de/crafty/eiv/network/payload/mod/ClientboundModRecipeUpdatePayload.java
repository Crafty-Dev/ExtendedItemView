package de.crafty.eiv.network.payload.mod;

import de.crafty.eiv.ExtendedItemView;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundModRecipeUpdatePayload(int types) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundModRecipeUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClientboundModRecipeUpdatePayload::types,
            ClientboundModRecipeUpdatePayload::new
    );

    public static final Type<ClientboundModRecipeUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "mod_recipe_update"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
