package de.crafty.eiv.recipe.item;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.extra.FluidStack;
import de.crafty.eiv.recipe.inventory.SlotContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidItem extends BlockItem {

    private final Fluid fluid;

    public FluidItem(Block block, FluidItemProperties properties) {
        super(block, properties);

        this.fluid = properties.fluid;
    }


    public Fluid getFluid() {
        return this.fluid;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);

        FluidStack fluidStack = FluidStack.fromItemStack(itemStack);
        list.add(Component.literal(String.valueOf(fluidStack.getAmount())).append(Component.translatable("eiv.fluid.unit")).withStyle(ChatFormatting.GRAY));

    }

    public static class FluidItemProperties extends Properties {

        private Fluid fluid = Fluids.EMPTY;

        public FluidItemProperties fluid(Fluid fluid) {
            this.fluid = fluid;
            return this;
        }

        public FluidItemProperties setItemId(ResourceKey<Item> id) {
            this.setId(id);
            return this;
        }

        @Override
        protected @NotNull String effectiveDescriptionId() {
            String s = super.effectiveDescriptionId();
            return s.replaceFirst("item.", "block.");
        }

        @Override
        public @NotNull ResourceLocation effectiveModel() {
            return ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "fluiditem");
        }

    }
}
