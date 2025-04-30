package de.crafty.eiv.api.recipe;

import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public interface IEivRecipeViewType {

    IEivRecipeViewType NONE = new IEivRecipeViewType() {

        @Override
        public Component getDisplayName() {
            return Component.empty();
        }


        @Override
        public int getDisplayWidth() {
            return 0;
        }

        @Override
        public int getDisplayHeight() {
            return 0;
        }

        @Override
        public ResourceLocation getGuiTexture() {
            return null;
        }

        @Override
        public int getSlotCount() {
            return 0;
        }

        @Override
        public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {
        }

        @Override
        public ResourceLocation getId() {
            return null;
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Items.BARRIER);
        }
    };

    Component getDisplayName();

    int getDisplayWidth();

    int getDisplayHeight();


    ResourceLocation getGuiTexture();

    int getSlotCount();

    void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition);

    ResourceLocation getId();


    ItemStack getIcon();

    default List<ItemStack> getCraftReferences() {
        return List.of();
    }
}
