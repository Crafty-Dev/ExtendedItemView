package de.crafty.eiv.api.recipe;

import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.recipe.rendering.AnimationTicker;
import de.crafty.eiv.recipe.vanilla.crafting.CraftingViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.SlotContent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public interface IEivViewRecipe {

    List<IEivViewRecipe> PLACEHOLDER = List.of(
            new IEivViewRecipe() {


                @Override
                public IEivRecipeViewType getViewType() {
                    return CraftingViewType.INSTANCE;
                }

                @Override
                public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
                }

                @Override
                public List<SlotContent> getIngredients() {
                    return List.of();
                }

                @Override
                public List<SlotContent> getResults() {
                    return List.of();
                }

            }
    );

    IEivRecipeViewType getViewType();

    void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext);

    List<SlotContent> getIngredients();

    List<SlotContent> getResults();

    default int getPriority() {
        return 0;
    }


    default List<AnimationTicker> getAnimationTickers() {
        return List.of();
    }

    default void renderRecipe(RecipeViewScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

    }


    default boolean supportsItemTransfer() {
        return false;
    }

    default void mapRecipeItems(AbstractContainerMenu menu, Inventory inventory) {
    }

}
