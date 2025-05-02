package de.crafty.eiv.extra;

import de.crafty.eiv.api.recipe.ItemViewRecipes;
import de.crafty.eiv.recipe.item.FluidItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidStack {

    public static final int AMOUNT_FULL = 1000;
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    private final Fluid fluid;
    private int amount;

    public FluidStack(final Fluid fluid) {
        this.fluid = fluid;
        this.amount = FluidStack.AMOUNT_FULL;
    }

    public FluidStack(final Fluid fluid, int amount){
        this.fluid = fluid;
        this.amount = amount;
    }

    public Fluid getFluid() {
        return this.fluid;
    }

    public int getAmount() {
        return this.amount;
    }

    public static FluidStack fromItemStack(ItemStack stack){
        if(!(stack.getItem() instanceof FluidItem fluidItem))
            return FluidStack.EMPTY;

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int amount = FluidStack.AMOUNT_FULL;
        if(tag.contains("fluidAmount"))
            amount = tag.getInt("fluidAmount").orElseGet(() -> FluidStack.AMOUNT_FULL);

        return new FluidStack(fluidItem.getFluid(), amount);
    }

    public ItemStack createItemStack(){
        Item item = ItemViewRecipes.INSTANCE.itemForFluid(this.fluid);
        if(item == Items.AIR)
            return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("fluidAmount", this.amount);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return stack;
    }
}
