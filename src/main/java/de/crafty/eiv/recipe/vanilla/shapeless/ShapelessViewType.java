package de.crafty.eiv.recipe.vanilla.shapeless;

import de.crafty.eiv.recipe.vanilla.crafting.CraftingViewType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ShapelessViewType extends CraftingViewType {

    public static final ShapelessViewType INSTANCE = new ShapelessViewType();

    @Override
    public Component getDisplayName() {
        return Component.translatable("view.eiv.type.shapeless");
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.withDefaultNamespace("crafting_shapeless");
    }
}
