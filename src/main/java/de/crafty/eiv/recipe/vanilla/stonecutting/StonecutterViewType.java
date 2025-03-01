package de.crafty.eiv.recipe.vanilla.stonecutting;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StonecutterViewType implements IEivRecipeViewType {

    protected static final StonecutterViewType INSTANCE = new StonecutterViewType();

    private static final ResourceLocation STONECUTTER_LOCATION = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/type/stonecutter.png");

    @Override
    public Component getDisplayName() {
        return Component.translatable("view.eiv.type.stonecutter");
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.withDefaultNamespace("stonecutting");
    }

    @Override
    public int getDisplayWidth() {
        return 74;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return STONECUTTER_LOCATION;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
        //Input
        slotDefinition.addItemSlot(0, 1, 1);

        //Result
        slotDefinition.addItemSlot(1, 57, 1);
    }

}
