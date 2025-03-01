package de.crafty.eiv;

import de.crafty.eiv.api.IExtendedItemViewIntegration;
import de.crafty.eiv.api.recipe.IEivServerModRecipe;
import de.crafty.eiv.api.recipe.ItemViewRecipes;
import de.crafty.eiv.recipe.inventory.SlotContent;
import de.crafty.eiv.recipe.vanilla.blasting.BlastingViewRecipe;
import de.crafty.eiv.recipe.vanilla.campfire.CampfireViewRecipe;
import de.crafty.eiv.recipe.vanilla.crafting.CraftingViewRecipe;
import de.crafty.eiv.recipe.vanilla.shapeless.ShapelessViewRecipe;
import de.crafty.eiv.recipe.vanilla.smelting.SmeltingViewRecipe;
import de.crafty.eiv.recipe.vanilla.smithing.SmithingViewRecipe;
import de.crafty.eiv.recipe.vanilla.smoking.SmokingViewRecipe;
import de.crafty.eiv.recipe.vanilla.stonecutting.StonecutterViewRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

public class BuiltInEivIntegration implements IExtendedItemViewIntegration {

    public static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/eiv_widgets.png");

    @Override
    public void onIntegrationInitialize() {

        ItemViewRecipes.INSTANCE.registerVanillaLikeWrapper(RecipeType.CRAFTING, vanillaLike -> {
            if (vanillaLike instanceof ShapedRecipe shapedRecipe)
                return List.of(new CraftingViewRecipe(shapedRecipe));

            if (vanillaLike instanceof ShapelessRecipe shapelessRecipe)
                return List.of(new ShapelessViewRecipe(shapelessRecipe));

            return List.of();
        });

        ItemViewRecipes.INSTANCE.registerVanillaLikeWrapper(RecipeType.SMELTING, vanillaLike -> List.of(new SmeltingViewRecipe((SmeltingRecipe) vanillaLike)));
        ItemViewRecipes.INSTANCE.registerVanillaLikeWrapper(RecipeType.BLASTING, vanillaLike -> List.of(new BlastingViewRecipe((BlastingRecipe) vanillaLike)));
        ItemViewRecipes.INSTANCE.registerVanillaLikeWrapper(RecipeType.SMOKING, vanillaLike -> List.of(new SmokingViewRecipe((SmokingRecipe) vanillaLike)));

        ItemViewRecipes.INSTANCE.registerVanillaLikeWrapper(RecipeType.STONECUTTING, vanillaLike -> List.of(new StonecutterViewRecipe((StonecutterRecipe) vanillaLike)));


        ItemViewRecipes.INSTANCE.registerVanillaLikeWrapper(RecipeType.SMITHING, vanillaLike -> {
            List<SmithingViewRecipe> recipes = new ArrayList<>();

            if (vanillaLike instanceof SmithingRecipe smithingRecipe) {
                if (smithingRecipe.templateIngredient().isPresent()) {
                    SlotContent.of(smithingRecipe.templateIngredient().get()).getValidContents().forEach(templateStack -> {
                        if(smithingRecipe.baseIngredient().isPresent()){
                            SlotContent.of(smithingRecipe.baseIngredient().get()).getValidContents().forEach(baseStack -> {
                                recipes.add(new SmithingViewRecipe(smithingRecipe, baseStack, templateStack));
                            });

                        }
                    });
                }
            }

            return recipes;
        });

        ItemViewRecipes.INSTANCE.registerVanillaLikeWrapper(RecipeType.CAMPFIRE_COOKING, vanillaLike -> List.of(new CampfireViewRecipe((CampfireCookingRecipe) vanillaLike)));



    }
}
