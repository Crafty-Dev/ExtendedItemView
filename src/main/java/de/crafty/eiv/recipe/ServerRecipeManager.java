package de.crafty.eiv.recipe;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.*;
import de.crafty.eiv.network.payload.ClientboundAllUpdatesFinishedPayload;
import de.crafty.eiv.network.payload.ClientboundGeneralUpdateStartedPayload;
import de.crafty.eiv.network.payload.mod.ClientboundModRecipeUpdatePayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdateEndPayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdatePayload;
import de.crafty.eiv.network.payload.mod.ClientboundModTypeUpdateStartPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdateEndPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdatePayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeTypeUpdateStartPayload;
import de.crafty.eiv.network.payload.vanillalike.ClientboundVanillaLikeRecipeUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.*;

//TODO block incoming requests while update sending
public class ServerRecipeManager {

    public static final ServerRecipeManager INSTANCE = new ServerRecipeManager();

    private static final HashMap<RecipeType<?>, List<VanillaRecipeEntry>> VANILLA_LIKE_RECIPES = new LinkedHashMap<>();
    private static final HashMap<ModRecipeType<?>, List<ModRecipeEntry>> MOD_RECIPES = new LinkedHashMap<>();

    private static final HashMap<TagKey<Item>, List<Item>> TAGS = new HashMap<>();

    private MinecraftServer server;

    private ServerRecipeManager() {

    }

    public void setServer(MinecraftServer server) {
        this.server = server;
        this.broadcastAllRecipes();
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public void reload(RecipeManager recipeManager) {
        ExtendedItemView.LOGGER.info("Reloading all Recipes...");


        this.reloadVanillaLikeRecipes(recipeManager);
        this.reloadModRecipes();

        this.broadcastAllRecipes();

    }

    public void reloadAndSendModOnly() {

        this.reloadModRecipes();
        this.broadcastModRecipes();
    }

    public void reloadAndSendVanillaOnly() {
        if (this.server == null) return;

        this.reloadVanillaLikeRecipes(this.server.getRecipeManager());
        this.broadcastVanillaRecipes();
    }

    private void reloadVanillaLikeRecipes(RecipeManager recipeManager) {
        VANILLA_LIKE_RECIPES.clear();

        for (RecipeHolder<?> recipe : recipeManager.getRecipes()) {
            List<VanillaRecipeEntry> list = VANILLA_LIKE_RECIPES.getOrDefault(recipe.value().getType(), new ArrayList<>());
            list.add(new VanillaRecipeEntry(recipe.id().location(), recipe.value()));
            VANILLA_LIKE_RECIPES.put(recipe.value().getType(), list);
        }
    }

    private void reloadModRecipes() {
        MOD_RECIPES.clear();

        List<IEivServerModRecipe> serverRecipes = new ArrayList<>();
        ItemViewRecipes.INSTANCE.getModRecipeProviders().forEach(serverModRecipeProvider -> {
            serverModRecipeProvider.provide(serverRecipes);
        });

        serverRecipes.forEach(iEivServerModRecipe -> {

            ResourceLocation typeId = iEivServerModRecipe.getRecipeType().getId();
            List<ModRecipeEntry> list = MOD_RECIPES.getOrDefault(iEivServerModRecipe.getRecipeType(), new ArrayList<>());
            list.add(new ModRecipeEntry(ResourceLocation.fromNamespaceAndPath(typeId.getNamespace(), typeId.getPath() + "/" + UUID.randomUUID()), iEivServerModRecipe));
            MOD_RECIPES.put(iEivServerModRecipe.getRecipeType(), list);
        });
    }

    private void broadcastAllRecipes() {
        if (this.server == null) {
            return;
        }
        ExtendedItemView.LOGGER.info("Broadcasting all recipes...");

        this.server.getPlayerList().getPlayers().forEach(this::informAboutAllRecipes);
    }

    private void broadcastVanillaRecipes() {
        if (this.server == null) {
            return;
        }
        ExtendedItemView.LOGGER.info("Broadcasting vanilla-like recipes...");

        this.server.getPlayerList().getPlayers().forEach(serverPlayer -> {
            ServerPlayNetworking.send(serverPlayer, new ClientboundGeneralUpdateStartedPayload());
            this.informAboutVanillaLikeRecipes(serverPlayer);
            ServerPlayNetworking.send(serverPlayer, new ClientboundAllUpdatesFinishedPayload());
        });
    }

    //TODO make possible to reload by modid
    private void broadcastModRecipes() {
        if (this.server == null) {
            return;
        }
        ExtendedItemView.LOGGER.info("Broadcasting Mod recipes...");

        this.server.getPlayerList().getPlayers().forEach(serverPlayer -> {
            ServerPlayNetworking.send(serverPlayer, new ClientboundGeneralUpdateStartedPayload());
            this.informAboutModRecipes(serverPlayer);
            ServerPlayNetworking.send(serverPlayer, new ClientboundAllUpdatesFinishedPayload());
        });
    }


    public void informAboutAllRecipes(ServerPlayer serverPlayer) {
        if (VANILLA_LIKE_RECIPES.isEmpty() && MOD_RECIPES.isEmpty())
            return;

        ServerPlayNetworking.send(serverPlayer, new ClientboundGeneralUpdateStartedPayload());
        this.informAboutVanillaLikeRecipes(serverPlayer);
        this.informAboutModRecipes(serverPlayer);
        ServerPlayNetworking.send(serverPlayer, new ClientboundAllUpdatesFinishedPayload());

    }

    private void informAboutVanillaLikeRecipes(ServerPlayer serverPlayer) {
        if (VANILLA_LIKE_RECIPES.isEmpty())
            return;

        ExtendedItemView.LOGGER.info("Informing {} about {} vanilla-like recipe types", serverPlayer.getName(), VANILLA_LIKE_RECIPES.size());
        ServerPlayNetworking.send(serverPlayer, new ClientboundVanillaLikeRecipeUpdatePayload(VANILLA_LIKE_RECIPES.size()));
        VANILLA_LIKE_RECIPES.forEach((recipeType, recipes) -> {
            ServerPlayNetworking.send(serverPlayer, new ClientboundVanillaLikeTypeUpdateStartPayload(recipeType, recipes.size()));
            recipes.forEach(recipe -> {
                ServerPlayNetworking.send(serverPlayer, new ClientboundVanillaLikeTypeUpdatePayload(recipe));
            });
            ServerPlayNetworking.send(serverPlayer, new ClientboundVanillaLikeTypeUpdateEndPayload(recipeType));
        });

    }

    private void informAboutModRecipes(ServerPlayer serverPlayer) {
        if (MOD_RECIPES.isEmpty())
            return;

        ExtendedItemView.LOGGER.info("Informing {} about {} mod recipe types", serverPlayer.getName(), MOD_RECIPES.size());
        ServerPlayNetworking.send(serverPlayer, new ClientboundModRecipeUpdatePayload(MOD_RECIPES.size()));
        MOD_RECIPES.forEach((type, entries) -> {
            ServerPlayNetworking.send(serverPlayer, new ClientboundModTypeUpdateStartPayload(type, entries.size()));
            entries.forEach(recipe -> {
                ServerPlayNetworking.send(serverPlayer, new ClientboundModTypeUpdatePayload(recipe));
            });
            ServerPlayNetworking.send(serverPlayer, new ClientboundModTypeUpdateEndPayload(type));
        });
    }


    public record VanillaRecipeEntry(ResourceLocation id, Recipe<?> recipe) {

        public static final StreamCodec<RegistryFriendlyByteBuf, VanillaRecipeEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                recipeEntry -> recipeEntry.id().toString(),
                Recipe.STREAM_CODEC,
                VanillaRecipeEntry::recipe,
                (s, r) -> new VanillaRecipeEntry(ResourceLocation.tryParse(s), r)
        );
    }

    public record ModRecipeEntry(ResourceLocation modRecipeId, IEivServerModRecipe recipe) {

        public static final StreamCodec<RegistryFriendlyByteBuf, ModRecipeEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                entry -> entry.modRecipeId().toString(),
                ByteBufCodecs.COMPOUND_TAG,
                ModRecipeEntry::createFullTag,
                (s, compoundTag) -> new ModRecipeEntry(ResourceLocation.tryParse(s), ModRecipeEntry.fromTag(compoundTag))
        );


        private CompoundTag createFullTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("recipeType", this.recipe().getRecipeType().getId().toString());
            CompoundTag dataTag = new CompoundTag();
            this.recipe().writeToTag(dataTag);
            tag.put("recipeData", dataTag);
            return tag;
        }

        private static IEivServerModRecipe fromTag(CompoundTag tag) {
            if (!tag.contains("recipeType", CompoundTag.TAG_STRING))
                return null;

            ModRecipeType<?> recipeType = ModRecipeType.byId(ResourceLocation.parse(tag.getString("recipeType")));
            if (recipeType == null)
                return null;

            IEivServerModRecipe modRecipe = recipeType.getEmptyConstructor().construct();
            modRecipe.loadFromTag(tag.getCompound("recipeData"));
            return modRecipe;
        }
    }


    //Transfer
    public void performRecipeTransfer(ServerPlayer player, HashMap<Integer, Integer> transferMap, HashMap<Integer, HashMap<Integer, ItemStack>> usedPlayerSlots) {

        if (!player.hasContainerOpen())
            return;

        transferMap.forEach((recipeSlot, destSlot) -> {

            HashMap<Integer, ItemStack> usedSlots = usedPlayerSlots.getOrDefault(recipeSlot, new HashMap<>());

            usedSlots.forEach((playerSlot, stack) -> {
                ItemStack currentInDest = player.containerMenu.getSlot(destSlot).getItem();

                if(currentInDest.isEmpty())
                    player.containerMenu.getSlot(destSlot).set(player.getInventory().removeItem(playerSlot, stack.getCount()));
                else
                    player.containerMenu.getSlot(destSlot).set(currentInDest.copyWithCount(currentInDest.getCount() + player.getInventory().removeItem(playerSlot, stack.getCount()).getCount()));

            });

        });

    }

}
