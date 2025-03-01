package de.crafty.eiv.recipe.vanilla.shapeless;

import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.SlotContent;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

public class ShapelessViewRecipe implements IEivViewRecipe {

    private final SlotContent result;
    private final List<SlotContent> ingredients;

    public ShapelessViewRecipe(ShapelessRecipe shapelessRecipe) {
        this.ingredients = new ArrayList<>();

        shapelessRecipe.ingredients.forEach(ingredient -> {
            this.ingredients.add(SlotContent.of(ingredient));
        });

        this.result = SlotContent.of(shapelessRecipe.result);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return ShapelessViewType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {

        for(int i = 0; i < ingredients.size() && i < this.getViewType().getSlotCount() - 1; i++) {
            slotFillContext.bindSlot(i, ingredients.get(i));
        }

        slotFillContext.bindSlot(9, this.result);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return this.ingredients;
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.result);
    }
}
