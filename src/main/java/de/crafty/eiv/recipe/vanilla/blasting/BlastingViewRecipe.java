package de.crafty.eiv.recipe.vanilla.blasting;

import de.crafty.eiv.BuiltInEivIntegration;
import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.recipe.inventory.SlotContent;
import de.crafty.eiv.recipe.rendering.AnimationTicker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.BlastingRecipe;

import java.util.List;

public class BlastingViewRecipe implements IEivViewRecipe {

    private final SlotContent input, result;
    private final AnimationTicker blastingTicker;

    public BlastingViewRecipe(BlastingRecipe blastingRecipe) {

        this.input = SlotContent.of(blastingRecipe.input());
        this.result = SlotContent.of(blastingRecipe.result);

        this.blastingTicker = AnimationTicker.create(ResourceLocation.withDefaultNamespace("blasting_ticker"), 100);
    }

    @Override
    public IEivRecipeViewType getViewType() {
        return BlastingViewType.INSTANCE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext slotFillContext) {
        slotFillContext.bindSlot(0, this.input);
        slotFillContext.bindSlot(2, this.result);
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
    public List<AnimationTicker> getAnimationTickers() {
        return List.of(this.blastingTicker);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int litProgress = Math.round(this.blastingTicker.getProgress() * 14);
        int smeltProgress = Math.round(this.blastingTicker.getProgress() * 24);

        guiGraphics.blit(RenderType::guiTextured, BuiltInEivIntegration.WIDGETS, 1, 20 + (14 - litProgress), 0, 14 - litProgress, 14, litProgress, 128, 128);

        guiGraphics.blit(RenderType::guiTextured, BuiltInEivIntegration.WIDGETS, 24, 19, 14, 0, smeltProgress, 16, 128, 128);
    }


    @Override
    public boolean supportsItemTransfer() {
        return true;
    }

    @Override
    public Class<? extends AbstractContainerScreen<?>> getTransferClass() {
        return BlastFurnaceScreen.class;
    }

    @Override
    public void mapRecipeItems(RecipeTransferMap transferMap) {

        transferMap.linkSlots(0, 0);

    }
}
