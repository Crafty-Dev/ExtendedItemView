package de.crafty.eiv.network.payload.transfer;

import de.crafty.eiv.ExtendedItemView;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundUpdateTransferCachePayload() implements CustomPacketPayload {


    public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateTransferCachePayload> STREAM_CODEC = CustomPacketPayload.codec(ClientboundUpdateTransferCachePayload::write, ClientboundUpdateTransferCachePayload::new);
    public static final Type<ClientboundUpdateTransferCachePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "update_transfer_cache"));

    public ClientboundUpdateTransferCachePayload(FriendlyByteBuf friendlyByteBuf) {
        this();
    }

    private void write(FriendlyByteBuf friendlyByteBuf){

    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
