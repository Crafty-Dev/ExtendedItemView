package de.crafty.eiv.recipe.vanilla.smithing;

import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.SlotContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import java.util.ArrayList;
import java.util.List;

public class SmithingViewRecipe implements IEivViewRecipe {


    private final SlotContent additionIngredient;
    private final ItemStack base, template;
    private final SlotContent result;

    private final boolean isTrimType;

    public SmithingViewRecipe(SmithingRecipe smithingRecipe, ItemStack base, ItemStack template) {
        this.isTrimType = smithingRecipe instanceof SmithingTrimRecipe;

        this.template = template;
        this.base = base;
        this.additionIngredient = smithingRecipe.additionIngredient().isPresent() ? SlotContent.of(smithingRecipe.additionIngredient().get()) : SlotContent.of(Items.AIR);

        if (Minecraft.getInstance().player == null) {
            this.result = SlotContent.of(Items.AIR);
            return;
        }

        HolderLookup.Provider provider = Minecraft.getInstance().player.clientLevel.registryAccess();

        if (smithingRecipe instanceof SmithingTrimRecipe) {
            List<ItemStack> possibleResults = new ArrayList<>();

            this.additionIngredient.getValidContents().forEach(addition -> {
                possibleResults.add(SmithingTrimRecipe.applyTrim(provider, this.base, addition, this.template));
            });

            this.result = SlotContent.of(possibleResults);

            return;
        }

        if (smithingRecipe instanceof SmithingTransformRecipe transformRecipe) {
            this.result = SlotContent.of(transformRecipe.result);
            return;
        }

        this.result = SlotContent.of(Items.AIR);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return SmithingViewType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {

        slotFillContext.bindSlot(0, SlotContent.of(this.template));
        slotFillContext.bindSlot(1, SlotContent.of(this.base));
        slotFillContext.bindSlot(2, this.additionIngredient);

        slotFillContext.bindDepedantSlot(3, this.additionIngredient::index, this.result);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of(SlotContent.of(this.template), SlotContent.of(this.base), this.additionIngredient);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.result);
    }

    @Override
    public int getPriority() {
        return this.isTrimType ? 1 : 0;
    }


    @Override
    public boolean supportsItemTransfer() {
        return true;
    }

    @Override
    public Class<? extends AbstractContainerScreen<?>> getTransferClass() {
        return SmithingScreen.class;
    }

    @Override
    public void mapRecipeItems(RecipeTransferMap transferMap) {

        transferMap.linkSlots(0, 0);
        transferMap.linkSlots(1, 1);
        transferMap.linkSlots(2, 2);

    }
}
