package de.crafty.eiv.recipe.vanilla.smelting;

import de.crafty.eiv.BuiltInEivIntegration;
import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import de.crafty.eiv.recipe.inventory.SlotContent;
import de.crafty.eiv.recipe.rendering.AnimationTicker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.List;

public class SmeltingViewRecipe implements IEivViewRecipe {

    private final SlotContent input, result;
    private final AnimationTicker smeltingTicker;

    public SmeltingViewRecipe(SmeltingRecipe recipe) {

        this.input = SlotContent.of(recipe.input());
        this.result = SlotContent.of(recipe.result);

        this.smeltingTicker = AnimationTicker.create(ResourceLocation.withDefaultNamespace("smelting_tick"), 200);
    }

    @Override
    public SmeltingViewType getViewType() {
        return SmeltingViewType.INSTANCE;
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
        return List.of(this.smeltingTicker);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        int litProgress = Math.round(this.smeltingTicker.getProgress() * 14);
        int smeltProgress = Math.round(this.smeltingTicker.getProgress() * 24);

        guiGraphics.blit(RenderType::guiTextured, BuiltInEivIntegration.WIDGETS, 1, 20 + (14 - litProgress), 0, 14 - litProgress, 14, litProgress, 128, 128);

        guiGraphics.blit(RenderType::guiTextured, BuiltInEivIntegration.WIDGETS, 24, 19, 14, 0, smeltProgress, 16, 128, 128);
    }
}
