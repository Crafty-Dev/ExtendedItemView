package de.crafty.eiv.recipe.vanilla.crafting;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CraftingViewType implements IEivRecipeViewType {

    public static final CraftingViewType INSTANCE = new CraftingViewType();

    private static final ResourceLocation CRAFTING_LOCATION = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/type/crafting.png");

    @Override
    public Component getDisplayName() {
        return Component.translatable("view.eiv.type.crafting");
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.withDefaultNamespace("shaped_crafting");
    }

    @Override
    public int getDisplayWidth() {
        return 116;
    }


    @Override
    public int getDisplayHeight() {
        return 54;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return CRAFTING_LOCATION;
    }


    @Override
    public int getSlotCount() {
        return 10;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {

        //Input slots
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                slotDefinition.addItemSlot(x + y * 3, 1 + x * 18,  1 + y * 18);
            }
        }

        //Result Slot
        slotDefinition.addItemSlot(9, 95, 19);

    }

}
