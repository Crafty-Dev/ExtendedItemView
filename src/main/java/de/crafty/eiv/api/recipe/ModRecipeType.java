package de.crafty.eiv.api.recipe;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public interface ModRecipeType<T extends IEivServerModRecipe> {

    HashMap<ResourceLocation, ModRecipeType<?>> MOD_RECIPE_TYPES = new HashMap<>();


    ResourceLocation getId();

    EmptyRecipeConstructor<T> getEmptyConstructor();

    static <S extends IEivServerModRecipe> ModRecipeType<S> register(ResourceLocation id, EmptyRecipeConstructor<S> emptyRecipeConstructor) {

        ModRecipeType<S> type = new ModRecipeType<S>() {
            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public EmptyRecipeConstructor<S> getEmptyConstructor() {
                return emptyRecipeConstructor;
            }


        };
        MOD_RECIPE_TYPES.put(id, type);
        return type;
    }

    static ModRecipeType<?> byId(ResourceLocation id){
        return MOD_RECIPE_TYPES.getOrDefault(id, null);
    }

    static ResourceLocation idFromType(ModRecipeType<?> recipeType) {
        return recipeType.getId();
    }



    interface EmptyRecipeConstructor<T extends IEivServerModRecipe> {

        T construct();

    }
}
