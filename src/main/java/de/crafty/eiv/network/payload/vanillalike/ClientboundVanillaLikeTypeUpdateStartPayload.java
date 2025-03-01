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

public record ClientboundVanillaLikeTypeUpdateStartPayload(RecipeType<?> recipeType, int recipeAmount) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundVanillaLikeTypeUpdateStartPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            clientboundRecipeInfoStart -> Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getKey(clientboundRecipeInfoStart.recipeType)).toString(),
            ByteBufCodecs.INT,
            ClientboundVanillaLikeTypeUpdateStartPayload::recipeAmount,
            (s, integer) -> new ClientboundVanillaLikeTypeUpdateStartPayload(BuiltInRegistries.RECIPE_TYPE.getValue(ResourceLocation.parse(s)), integer)
    );

    public static final Type<ClientboundVanillaLikeTypeUpdateStartPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "vlt_update_start"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
