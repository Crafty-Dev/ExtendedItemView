package de.crafty.eiv.api.recipe;

import com.nimbusds.jose.util.Resource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemViewRecipes {

    public static final ItemViewRecipes INSTANCE = new ItemViewRecipes();


    private final HashMap<RecipeType<?>, ClientVanillaRecipeWrapper> clientWrapperMap;
    private final HashMap<ModRecipeType<?>, ClientModRecipeWrapper> modRecipeWrapperMap;

    private final List<ServerModRecipeProvider> modRecipeProviders;

    private final HashMap<Fluid, Item> fluidItemMap;


    private ItemViewRecipes() {
        this.clientWrapperMap = new HashMap<>();
        this.modRecipeWrapperMap = new HashMap<>();

        this.modRecipeProviders = new ArrayList<>();

        this.fluidItemMap = new HashMap<>();
    }


    public void registerVanillaLikeWrapper(RecipeType<?> recipeType, ClientVanillaRecipeWrapper wrapper) {
        this.clientWrapperMap.put(recipeType, wrapper);
    }

    public void registerModRecipeWrapper(ModRecipeType<?> recipeType, ClientModRecipeWrapper wrapper) {
        this.modRecipeWrapperMap.put(recipeType, wrapper);
    }

    public void addModRecipeProvider(ServerModRecipeProvider provider) {
        this.modRecipeProviders.add(provider);
    }



    public HashMap<RecipeType<?>, ClientVanillaRecipeWrapper> getVanillaWrapperMap() {
        return this.clientWrapperMap;
    }

    public HashMap<ModRecipeType<?>, ClientModRecipeWrapper> getModRecipeWrapperMap() {
        return this.modRecipeWrapperMap;
    }

    public List<ServerModRecipeProvider> getModRecipeProviders() {
        return this.modRecipeProviders;
    }

    public void setFluidItemMap(HashMap<Fluid, Item> fluidItemMap) {
        this.fluidItemMap.clear();
        this.fluidItemMap.putAll(fluidItemMap);
    }

    public Item itemForFluid(Fluid fluid) {
        return this.fluidItemMap.getOrDefault(fluid, Items.AIR);
    }



    public interface ClientVanillaRecipeWrapper {

        List<? extends IEivViewRecipe> wrap(Recipe<?> vanillaLike);

    }

    public interface ClientModRecipeWrapper {

        List<? extends IEivViewRecipe> wrap(IEivServerModRecipe modRecipe);

    }

    public interface ServerModRecipeProvider {

        void provide(List<IEivServerModRecipe> recipeList);

    }
}
