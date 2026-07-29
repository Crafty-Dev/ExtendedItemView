package de.crafty.eiv.common.builtin.shapeless;

import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.builtin.transmute.TransmuteServerRecipe;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class ShapelessViewRecipe implements IEivViewRecipe {

    private final SlotContent result;
    private final List<SlotContent> ingredients;

    public ShapelessViewRecipe(ShapelessServerRecipe shapelessRecipe) {
        this.ingredients = new ArrayList<>();

        for (int i = 0; i < shapelessRecipe.getIngredients().size(); i++) {
            Ingredient ingredient = shapelessRecipe.getIngredients().get(i);
            List<ItemStack> exact = shapelessRecipe.getExactIngredients() != null && i < shapelessRecipe.getExactIngredients().size() ? shapelessRecipe.getExactIngredients().get(i) : null;
            if (exact != null && !exact.isEmpty()) {
                this.ingredients.add(SlotContent.of(exact));
            } else {
                this.ingredients.add(SlotContent.of(ingredient));
            }
        }

        this.result = SlotContent.of(shapelessRecipe.getResult());
    }

    private ShapelessViewRecipe(SlotContent result, List<SlotContent> ingredients) {
        this.result = result;
        this.ingredients = ingredients;
    }

    public ShapelessViewRecipe(TransmuteServerRecipe transmuteRecipe) {
        this.ingredients = new ArrayList<>();
        this.ingredients.add(SlotContent.of(transmuteRecipe.getInput()));
        this.ingredients.add(SlotContent.of(transmuteRecipe.getMaterial()));

        this.result = SlotContent.of(transmuteRecipe.getResults());
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return ShapelessViewType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {

        for (int i = 0; i < ingredients.size() && i < this.getViewType().getSlotCount() - 1; i++) {
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
    public void mapRecipeItems(RecipeTransferMap transferMap, AbstractContainerScreen<?> screen) {

        if (screen instanceof InventoryScreen invScreen) {
            transferMap.linkSlots(0, 1);
            transferMap.linkSlots(1, 2);
            transferMap.linkSlots(3, 3);
            transferMap.linkSlots(4, 4);
            return;
        }

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
    public List<Class<? extends AbstractContainerScreen<?>>> getTransferClasses() {
        return List.of(CraftingScreen.class, InventoryScreen.class);
    }

    @Override
    public boolean canTransferToScreen(AbstractContainerScreen<?> screen) {
        return screen instanceof CraftingScreen || this.ingredients.size() <= 4;
    }


    @Override
    public IEivViewRecipe asChatCopy() {
        List<SlotContent> ingredients = this.ingredients.stream().map(SlotContent::copy).toList();
        return new  ShapelessViewRecipe(this.result.copy(), ingredients);
    }
}
