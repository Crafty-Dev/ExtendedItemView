package de.crafty.eiv.network.payload.transfer;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.recipe.ServerRecipeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record ServerboundTransferPayload(HashMap<Integer, Integer> transferMap,
                                         HashMap<Integer, HashMap<Integer, ItemStack>> usedPlayerSlots) implements CustomPacketPayload {


    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTransferPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ServerboundTransferPayload::encodeMap,
            ServerboundTransferPayload::decodeMap

    );

    public static final Type<ServerboundTransferPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "recipe_transfer"));


    private CompoundTag encodeMap() {
        CompoundTag encoded = new CompoundTag();

        CompoundTag transferMap = new CompoundTag();
        this.transferMap.forEach((recipeSlot, destSlot) -> {
            transferMap.putInt(String.valueOf(recipeSlot), destSlot);
        });
        encoded.put("transferMap", transferMap);

        CompoundTag usedPlayerSlots = new CompoundTag();
        this.usedPlayerSlots.forEach((recipeSlot, usedSlots) -> {

            CompoundTag playerSlotsTag = new CompoundTag();
            usedSlots.forEach((playerSlot, stack) -> {
                playerSlotsTag.put(String.valueOf(playerSlot), stack.saveOptional(Minecraft.getInstance().level.registryAccess()));
            });

            usedPlayerSlots.put(String.valueOf(recipeSlot), playerSlotsTag);
        });

        encoded.put("usedPlayerSlots", usedPlayerSlots);
        return encoded;
    }

    private static ServerboundTransferPayload decodeMap(CompoundTag encoded) {

        HashMap<Integer, Integer> transferMap = new HashMap<>();
        CompoundTag encodedTransferMap = encoded.getCompound("transferMap");

        encodedTransferMap.getAllKeys().forEach(recipeSlot -> {
            transferMap.put(Integer.valueOf(recipeSlot), encodedTransferMap.getInt(recipeSlot));
        });

        HashMap<Integer, HashMap<Integer, ItemStack>> usedPlayerSlots = new HashMap<>();
        CompoundTag encodedUsedPlayerSlots = encoded.getCompound("usedPlayerSlots");

        encodedUsedPlayerSlots.getAllKeys().forEach(recipeSlot -> {
            HashMap<Integer, ItemStack> usedSlots = new HashMap<>();

            CompoundTag playerSlotsTag = encodedUsedPlayerSlots.getCompound(recipeSlot);
            playerSlotsTag.getAllKeys().forEach(playerSlot -> {

                ItemStack stack = ItemStack.parseOptional(ServerRecipeManager.INSTANCE.getServer().registryAccess(), playerSlotsTag.getCompound(playerSlot));
                usedSlots.put(Integer.valueOf(playerSlot), stack);
            });

            usedPlayerSlots.put(Integer.valueOf(recipeSlot), usedSlots);
        });

        return new ServerboundTransferPayload(transferMap, usedPlayerSlots);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
