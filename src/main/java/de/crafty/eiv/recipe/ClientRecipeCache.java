package de.crafty.eiv.recipe;

import de.crafty.eiv.api.recipe.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientRecipeCache {

    public static final ClientRecipeCache INSTANCE = new ClientRecipeCache();


    private final LinkedHashMap<RecipeType<?>, List<ServerRecipeManager.VanillaRecipeEntry>> vanillaLikeMap;
    private final LinkedHashMap<ModRecipeType<?>, List<ServerRecipeManager.ModRecipeEntry>> modMap;

    private final HashMap<ResourceLocation, List<ResourceLocation>> multiRecipeMap;
    private final HashMap<ResourceLocation, IEivViewRecipe> recipeMap;
    private final HashMap<Item, List<ResourceLocation>> byItemIngredient, byItemResult;


    private ClientRecipeCache() {
        this.vanillaLikeMap = new LinkedHashMap<>();
        this.modMap = new LinkedHashMap<>();

        this.multiRecipeMap = new LinkedHashMap<>();
        this.recipeMap = new HashMap<>();
        this.byItemIngredient = new HashMap<>();
        this.byItemResult = new HashMap<>();

    }


    public void updateVanillaLikeType(RecipeType<?> type, List<ServerRecipeManager.VanillaRecipeEntry> recipes) {
        this.vanillaLikeMap.getOrDefault(type, new ArrayList<>()).forEach(entry -> {
            this.multiRecipeMap.getOrDefault(entry.id(), new ArrayList<>()).forEach(resourceLocation -> {
                this.recipeMap.remove(resourceLocation);

                this.byItemIngredient.forEach((item, resourceLocations) -> {
                    resourceLocations.remove(resourceLocation);
                });

                this.byItemResult.forEach((item, resourceLocations) -> {
                    resourceLocations.remove(resourceLocation);
                });
            });
        });

        this.vanillaLikeMap.put(type, recipes);
    }

    public void updateModType(ModRecipeType<?> type, List<ServerRecipeManager.ModRecipeEntry> recipes) {
        this.modMap.getOrDefault(type, new ArrayList<>()).forEach(entry -> {
            this.multiRecipeMap.getOrDefault(entry.modRecipeId(), new ArrayList<>()).forEach(resourceLocation -> {
                this.recipeMap.remove(resourceLocation);

                this.byItemIngredient.forEach((item, resourceLocations) -> {
                    resourceLocations.remove(resourceLocation);
                });

                this.byItemResult.forEach((item, resourceLocations) -> {
                    resourceLocations.remove(resourceLocation);
                });
            });
        });

        this.modMap.put(type, recipes);
    }


    public List<IEivViewRecipe> getRecipesForCraftingInput(ItemStack inputStack) {
        List<IEivViewRecipe> recipes = new ArrayList<>();
        this.byItemIngredient.getOrDefault(inputStack.getItem(), List.of()).forEach(resourceLocation -> {
            recipes.add(this.recipeMap.get(resourceLocation));
        });
        return recipes;
    }

    public List<IEivViewRecipe> getRecipesForCraftingOutput(ItemStack outputStack) {
        List<IEivViewRecipe> recipes = new ArrayList<>();
        this.byItemResult.getOrDefault(outputStack.getItem(), List.of()).forEach(resourceLocation -> {
            recipes.add(this.recipeMap.get(resourceLocation));
        });
        return recipes;
    }

    public void sortVanillaLikeType(RecipeType<?> type) {
        ItemViewRecipes.ClientVanillaRecipeWrapper wrapper = ItemViewRecipes.INSTANCE.getVanillaWrapperMap().getOrDefault(type, null);

        if (wrapper == null || !this.vanillaLikeMap.containsKey(type))
            return;


        for (ServerRecipeManager.VanillaRecipeEntry vanillaLike : this.vanillaLikeMap.get(type)) {
            List<? extends IEivViewRecipe> wrappedRecipes = wrapper.wrap(vanillaLike.recipe());
            if (wrappedRecipes.isEmpty())
                continue;

            for (int id = 0; id < wrappedRecipes.size(); id++) {
                IEivViewRecipe wrapped = wrappedRecipes.get(id);

                ResourceLocation uniqueId = this.getUniqueId(vanillaLike, id);
                List<ResourceLocation> summarized = this.multiRecipeMap.getOrDefault(vanillaLike.id(), new ArrayList<>());
                summarized.add(uniqueId);
                this.multiRecipeMap.put(vanillaLike.id(), summarized);

                this.recipeMap.put(uniqueId, wrapped);

                wrapped.getIngredients().forEach(ingredient -> {
                    ingredient.getValidContents().forEach(stack -> {

                        List<ResourceLocation> byIngredient = this.byItemIngredient.getOrDefault(stack.getItem(), new ArrayList<>());
                        byIngredient.remove(uniqueId);
                        byIngredient.add(uniqueId);
                        this.byItemIngredient.put(stack.getItem(), byIngredient);
                    });
                });
                wrapped.getViewType().getCraftReferences().forEach(reference -> {
                    List<ResourceLocation> byIngredient = this.byItemIngredient.getOrDefault(reference.getItem(), new ArrayList<>());
                    byIngredient.remove(uniqueId);
                    byIngredient.add(uniqueId);
                    this.byItemIngredient.put(reference.getItem(), byIngredient);
                });



                wrapped.getResults().forEach(result -> {
                    result.getValidContents().forEach(stack -> {
                        List<ResourceLocation> byResult = this.byItemResult.getOrDefault(stack.getItem(), new ArrayList<>());
                        byResult.remove(uniqueId);
                        byResult.add(uniqueId);
                        this.byItemResult.put(stack.getItem(), byResult);
                    });
                });
            }
        }

    }

    public void sortModType(ModRecipeType<?> type) {
        ItemViewRecipes.ClientModRecipeWrapper wrapper = ItemViewRecipes.INSTANCE.getModRecipeWrapperMap().getOrDefault(type, null);

        if (wrapper == null || !this.modMap.containsKey(type))
            return;


        for (ServerRecipeManager.ModRecipeEntry modEntry : this.modMap.get(type)) {
            List<? extends IEivViewRecipe> wrappedRecipes = wrapper.wrap(modEntry.recipe());
            if (wrappedRecipes.isEmpty())
                continue;

            for (int id = 0; id < wrappedRecipes.size(); id++) {
                IEivViewRecipe wrapped = wrappedRecipes.get(id);

                ResourceLocation uniqueId = this.getUniqueId(modEntry, id);
                List<ResourceLocation> summarized = this.multiRecipeMap.getOrDefault(modEntry.modRecipeId(), new ArrayList<>());
                summarized.add(uniqueId);
                this.multiRecipeMap.put(modEntry.modRecipeId(), summarized);

                this.recipeMap.put(uniqueId, wrapped);

                wrapped.getIngredients().forEach(ingredient -> {
                    ingredient.getValidContents().forEach(stack -> {

                        List<ResourceLocation> byIngredient = this.byItemIngredient.getOrDefault(stack.getItem(), new ArrayList<>());
                        byIngredient.remove(uniqueId);
                        byIngredient.add(uniqueId);
                        this.byItemIngredient.put(stack.getItem(), byIngredient);
                    });
                });

                wrapped.getViewType().getCraftReferences().forEach(reference -> {
                    List<ResourceLocation> byIngredient = this.byItemIngredient.getOrDefault(reference.getItem(), new ArrayList<>());
                    byIngredient.remove(uniqueId);
                    byIngredient.add(uniqueId);
                    this.byItemIngredient.put(reference.getItem(), byIngredient);
                });

                wrapped.getResults().forEach(result -> {
                    result.getValidContents().forEach(stack -> {
                        List<ResourceLocation> byResult = this.byItemResult.getOrDefault(stack.getItem(), new ArrayList<>());
                        byResult.remove(uniqueId);
                        byResult.add(uniqueId);
                        this.byItemResult.put(stack.getItem(), byResult);
                    });
                });
            }
        }
    }


    private ResourceLocation getUniqueId(ServerRecipeManager.VanillaRecipeEntry vanillaLike, int index) {
        return ResourceLocation.fromNamespaceAndPath(vanillaLike.id().getNamespace(), vanillaLike.id().getPath() + "/" + index);
    }

    private ResourceLocation getUniqueId(ServerRecipeManager.ModRecipeEntry modEntry, int index) {
        return ResourceLocation.fromNamespaceAndPath(modEntry.modRecipeId().getNamespace(), modEntry.modRecipeId().getPath() + "/" + index);
    }

}
