package de.crafty.eiv.common.builtin.shapeless;

import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.recipe.util.EivTagUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class ShapelessServerRecipe implements IEivServerRecipe {

    public static final EivRecipeType<ShapelessServerRecipe> TYPE = EivRecipeType.register(
            Identifier.withDefaultNamespace("shapeless_crafting"),
            () -> new ShapelessServerRecipe(List.of(), ItemStack.EMPTY)
    );

    private List<Ingredient> ingredients;
    private List<List<ItemStack>> exactIngredients;
    private ItemStack result;

    public ShapelessServerRecipe(List<Ingredient> ingredients, ItemStack result) {
        this.ingredients = ingredients;
        this.result = result;
    }


    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public List<List<ItemStack>> getExactIngredients() {
        return this.exactIngredients;
    }

    public ItemStack getResult() {
        return this.result;
    }


    @Override
    public void writeToTag(CompoundTag tag) {

        tag.put("ingredients", EivTagUtil.writeList(this.ingredients, (origin, tag1) -> EivTagUtil.writeIngredient(origin)));
        tag.put("result", EivTagUtil.encodeItemStackOnServer(this.result));
    }

    @Override
    public void loadFromTag(CompoundTag tag) {

        this.ingredients = EivTagUtil.readList(tag, "ingredients", EivTagUtil::readIngredient);
        this.result = EivTagUtil.decodeItemStackOnClient(tag.getCompound("result").orElseGet(CompoundTag::new));

        this.exactIngredients = new java.util.ArrayList<>();
        ListTag listTag = tag.getListOrEmpty("ingredients");
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag ingredientTag = (CompoundTag) listTag.get(i);
            if (ingredientTag.contains("exact_stacks")) {
                List<ItemStack> stacks = new java.util.ArrayList<>();
                ingredientTag.getListOrEmpty("exact_stacks").forEach(itemTag -> {
                    stacks.add(EivTagUtil.decodeItemStackOnClient((CompoundTag) itemTag));
                });
                this.exactIngredients.add(stacks);
            } else {
                this.exactIngredients.add(null);
            }
        }
    }

    @Override
    public EivRecipeType<? extends IEivServerRecipe> getRecipeType() {
        return TYPE;
    }
}
