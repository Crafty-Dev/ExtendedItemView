package de.crafty.eiv.recipe.vanilla.stonecutting;

import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.SlotContent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.List;

public class StonecutterViewRecipe implements IEivViewRecipe {


    private final SlotContent input, result;

    public StonecutterViewRecipe(StonecutterRecipe stonecutterRecipe) {
        this.input = SlotContent.of(stonecutterRecipe.input());
        this.result = SlotContent.of(stonecutterRecipe.result);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return StonecutterViewType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        slotFillContext.bindSlot(0, this.input);
        slotFillContext.bindSlot(1, this.result);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(this.input);
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
        return StonecutterScreen.class;
    }

    @Override
    public void mapRecipeItems(RecipeTransferMap transferMap) {

        transferMap.linkSlots(0, 0);

    }
}
