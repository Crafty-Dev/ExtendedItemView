package de.crafty.eiv.api.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IEivServerModRecipe {


    void writeToTag(CompoundTag tag);

    void loadFromTag(CompoundTag tag);


    ModRecipeType<? extends IEivServerModRecipe> getRecipeType();
}
