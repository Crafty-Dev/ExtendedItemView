package de.crafty.eiv.recipe.vanilla.shapeless;

import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.SlotContent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
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


    @Override
    public boolean supportsItemTransfer() {
        return true;
    }

    @Override
    public void mapRecipeItems(RecipeTransferMap transferMap) {

        transferMap.linkSlots(0, 1);
        transferMap.linkSlots(1, 2);
        transferMap.linkSlots(2, 3);
        transferMap.linkSlots(3, 4);
        transferMap.linkSlots(4, 5);
        transferMap.linkSlots(5, 6);
        transferMap.linkSlots(6, 7);
        transferMap.linkSlots(7, 8);
        transferMap.linkSlots(8, 9);

    }

    @Override
    public Class<? extends AbstractContainerScreen<?>> getTransferClass() {
        return CraftingScreen.class;
    }
}
