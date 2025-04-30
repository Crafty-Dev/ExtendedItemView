package de.crafty.eiv.recipe.vanilla.smithing;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SmithingRecipeViewType implements IEivRecipeViewType {

    protected static final SmithingRecipeViewType INSTANCE = new SmithingRecipeViewType();

    private static final ResourceLocation SMITHING_LOCATION = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/type/smithing.png");

    @Override
    public Component getDisplayName() {
        return Component.translatable("view.eiv.type.smithing");
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.withDefaultNamespace("smithing");
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.SMITHING_TABLE);
    }

    @Override
    public int getDisplayWidth() {
        return 108;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return SMITHING_LOCATION;
    }

    @Override
    public int getSlotCount() {
        return 4;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition slotDefinition) {

        //Base
        slotDefinition.addItemSlot(0, 1, 1);

        //Addition
        slotDefinition.addItemSlot(1, 19, 1);

        //Template
        slotDefinition.addItemSlot(2, 37, 1);

        //Result
        slotDefinition.addItemSlot(3, 91, 1);
    }
}
