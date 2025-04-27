package de.crafty.eiv.recipe.inventory;

import com.mojang.datafixers.util.Either;
import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.ItemViewRecipes;
import de.crafty.eiv.extra.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotContent {

    private final List<ItemStack> content;
    private int current;

    private TagKey<Item> itemTag;

    private ItemStack itemOrigin;

    private Type type;

    private SlotContent(List<ItemStack> content) {
        this.content = content;
        this.current = 0;

        this.itemOrigin = ItemStack.EMPTY;

        this.type = Type.INGREDIENT;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    private SlotContent bindItemTag(TagKey<Item> tag) {
        this.itemTag = tag;
        this.setDataComponent();
        return this;
    }


    public void bindOrigin(ItemStack stack) {
        this.itemOrigin = stack;
    }

    public int size() {
        return this.content.size();
    }

    public boolean isEmpty() {
        return this.content.stream().filter(ItemStack::isEmpty).count() == this.content.size();
    }

    public int index() {

        if (this.hasItem(this.itemOrigin.getItem())) {
            for (int i = 0; i < this.size(); i++) {
                if (this.getByIndex(i).getItem() == this.itemOrigin.getItem())
                    return i;

            }
        }

        return this.current;
    }

    public ItemStack getByIndex(int index) {
        return this.content.isEmpty() ? ItemStack.EMPTY : this.content.get(index);
    }

    public ItemStack next() {
        this.current++;
        if (this.current >= this.content.size())
            this.current = 0;

        return this.getByIndex(this.index());
    }

    public void resetPointer() {
        this.current = 0;
        this.itemOrigin = ItemStack.EMPTY;
    }


    public List<ItemStack> getValidContents() {
        return this.content;
    }

    private void setDataComponent() {
        if (this.itemTag().isEmpty())
            return;

        this.content.forEach(stack -> {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putString(ExtendedItemView.MODID + "_recipeTag", this.itemTag().get().location().toString());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        });

    }

    public boolean hasItem(Item item) {
        return this.content.stream().anyMatch(stack -> stack.getItem() == item);
    }


    public Optional<TagKey<Item>> itemTag() {
        return this.itemTag == null ? Optional.empty() : Optional.of(this.itemTag);
    }

    public static SlotContent of(Item item) {
        return new SlotContent(List.of(new ItemStack(item)));
    }

    public static SlotContent ofItemList(List<Item> items) {
        List<ItemStack> stacks = new ArrayList<>();
        items.forEach(item -> stacks.add(new ItemStack(item)));
        return SlotContent.of(stacks);
    }

    public static SlotContent of(FluidStack fluidStack) {
        return new SlotContent(List.of(fluidStack.createItemStack()));
    }

    public static SlotContent ofFluidList(List<FluidStack> fluidStacks) {
        List<ItemStack> stacks = new ArrayList<>();
        fluidStacks.forEach(fluidStack -> stacks.add(fluidStack.createItemStack()));
        return new SlotContent(stacks);
    }

    public static SlotContent of(ItemStack stack) {
        return new SlotContent(List.of(stack));
    }

    public static SlotContent of(List<ItemStack> stacks) {
        return new SlotContent(stacks);
    }

    public static SlotContent of(TagKey<Item> itemTag) {
        List<Item> items = new ArrayList<>();
        BuiltInRegistries.ITEM.get(itemTag).ifPresent(holders -> {
            holders.forEach(holder -> {
                items.add(holder.value());
            });
        });

        List<ItemStack> stacks = new ArrayList<>();
        items.forEach(item -> stacks.add(new ItemStack(item)));

        return new SlotContent(stacks).bindItemTag(itemTag);
    }

    public static SlotContent of(Ingredient ingredient) {

        Either<TagKey<Item>, List<Holder<Item>>> ingredientContent = ingredient.values.unwrap();

        if (ingredientContent.right().isPresent()) {
            List<ItemStack> stacks = new ArrayList<>();
            ingredientContent.right().get().forEach(holder -> stacks.add(new ItemStack(holder.value())));
            return new SlotContent(stacks);
        }

        return ingredientContent.left().isPresent() ? SlotContent.of(ingredientContent.left().get()) : SlotContent.of(Items.AIR);

    }


    public enum Type {
        INGREDIENT,
        RESULT
    }

}
