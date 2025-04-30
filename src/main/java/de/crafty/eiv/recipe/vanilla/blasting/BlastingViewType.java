package de.crafty.eiv.recipe.vanilla.blasting;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.recipe.vanilla.smelting.SmeltingViewType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class BlastingViewType extends SmeltingViewType {

    public static final BlastingViewType INSTANCE = new BlastingViewType();

    private static final ResourceLocation BLASTING_LOCATION = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/type/blasting.png");

    @Override
    public Component getDisplayName() {
        return Component.translatable("view.eiv.type.blasting");
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return BLASTING_LOCATION;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.BLAST_FURNACE);
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "furnace_blasting");
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(new ItemStack(Items.BLAST_FURNACE));
    }
}
