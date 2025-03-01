package de.crafty.eiv.recipe.vanilla.smoking;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.recipe.vanilla.smelting.SmeltingViewType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SmokingViewType extends SmeltingViewType {

    public static final SmokingViewType INSTANCE = new SmokingViewType();

    private static final ResourceLocation BLASTING_LOCATION = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/type/smoking.png");

    @Override
    public Component getDisplayName() {
        return Component.translatable("view.eiv.type.smoking");
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return BLASTING_LOCATION;
    }
}
