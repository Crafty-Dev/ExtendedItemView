package de.crafty.eiv.network.payload.vanillalike;

import de.crafty.eiv.ExtendedItemView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ClientboundVanillaLikeTypeUpdateEndPayload(RecipeType<?> recipeType) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundVanillaLikeTypeUpdateEndPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            payload -> Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getKey(payload.recipeType)).toString(),
            s -> new ClientboundVanillaLikeTypeUpdateEndPayload(BuiltInRegistries.RECIPE_TYPE.getValue(ResourceLocation.parse(s)))
    );

    public static final Type<ClientboundVanillaLikeTypeUpdateEndPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "vlt_update_end"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
