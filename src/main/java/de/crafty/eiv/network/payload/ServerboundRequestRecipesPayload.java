package de.crafty.eiv.network.payload;

import de.crafty.eiv.ExtendedItemView;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ServerboundRequestRecipesPayload() implements CustomPacketPayload {

    public static final StreamCodec<FriendlyByteBuf, ServerboundRequestRecipesPayload> STREAM_CODEC = CustomPacketPayload.codec(ServerboundRequestRecipesPayload::write, ServerboundRequestRecipesPayload::new);
    public static final CustomPacketPayload.Type<ServerboundRequestRecipesPayload> TYPE = new CustomPacketPayload.Type<ServerboundRequestRecipesPayload>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "recipe_request"));

    private ServerboundRequestRecipesPayload(FriendlyByteBuf friendlyByteBuf) {
        this();
    }

    private void write(FriendlyByteBuf friendlyByteBuf){

    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
