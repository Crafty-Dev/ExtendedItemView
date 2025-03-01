package de.crafty.eiv.network.payload.mod;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.api.recipe.ItemViewRecipes;
import de.crafty.eiv.api.recipe.ModRecipeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundModTypeUpdateEndPayload(ModRecipeType<?> recipeType) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundModTypeUpdateEndPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            payload -> payload.recipeType().getId().toString(),
            s -> new ClientboundModTypeUpdateEndPayload(ModRecipeType.byId(ResourceLocation.tryParse(s)))
    );

    public static final Type<ClientboundModTypeUpdateEndPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "mt_update_end"));


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
