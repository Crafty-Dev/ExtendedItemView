package de.crafty.eiv.recipe.vanilla.campfire;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CampfireViewType implements IEivRecipeViewType {

    protected static final CampfireViewType INSTANCE = new CampfireViewType();

    private static final ResourceLocation CAMPFIRE_LOCATION = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/type/campfire.png");

    @Override
    public Component getDisplayName() {
        return Component.translatable("view.eiv.type.campfire");
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.withDefaultNamespace("campfire_cooking");
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.CAMPFIRE);
    }

    @Override
    public int getDisplayWidth() {
        return 74;
    }

    @Override
    public int getDisplayHeight() {
        return 36;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return CAMPFIRE_LOCATION;
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {

        //Ingredient
        slotDefinition.addItemSlot(0, 1, 1);

        //Cooked result
        slotDefinition.addItemSlot(1, 57, 1);
    }

}
