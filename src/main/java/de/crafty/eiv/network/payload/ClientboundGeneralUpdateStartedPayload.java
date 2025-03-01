package de.crafty.eiv.network.payload;

import de.crafty.eiv.ExtendedItemView;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundGeneralUpdateStartedPayload() implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundGeneralUpdateStartedPayload> STREAM_CODEC = CustomPacketPayload.codec(ClientboundGeneralUpdateStartedPayload::write, ClientboundGeneralUpdateStartedPayload::new);
    public static final Type<ClientboundGeneralUpdateStartedPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "general_update_started"));

    public ClientboundGeneralUpdateStartedPayload(FriendlyByteBuf friendlyByteBuf){
        this();
    }

    private void write(FriendlyByteBuf friendlyByteBuf){

    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
