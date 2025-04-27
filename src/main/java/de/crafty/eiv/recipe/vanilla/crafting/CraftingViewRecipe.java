package de.crafty.eiv.recipe.vanilla.crafting;

import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.SlotContent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.HashMap;
import java.util.List;

public class CraftingViewRecipe implements IEivViewRecipe {

    private final HashMap<Integer, SlotContent> ingredientSlotContents = new HashMap<>();
    private final SlotContent result;

    public CraftingViewRecipe(ShapedRecipe recipe) {


        int i = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {

                if (x >= recipe.getWidth() || y >= recipe.getHeight()) {
                    continue;
                }

                if (recipe.getIngredients().get(i).isPresent())
                    this.ingredientSlotContents.put(x + y * 3, SlotContent.of(recipe.getIngredients().get(i).get()));

                i++;
            }
        }

        this.result = SlotContent.of(recipe.result);


    }

    @Override
    public IEivRecipeViewType getViewType() {
        return CraftingViewType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        this.ingredientSlotContents.forEach(slotFillContext::bindSlot);
        slotFillContext.bindSlot(9, this.result);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return this.ingredientSlotContents.values().stream().toList();
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
    public Class<? extends AbstractContainerScreen<?>> getTransferClass() {
        return CraftingScreen.class;
    }

    @Override
    public void mapRecipeItems(RecipeTransferMap map) {

        map.linkSlots(0, 1);
        map.linkSlots(1, 2);
        map.linkSlots(2, 3);
        map.linkSlots(3, 4);
        map.linkSlots(4, 5);
        map.linkSlots(5, 6);
        map.linkSlots(6, 7);
        map.linkSlots(7, 8);
        map.linkSlots(8, 9);

    }
}
