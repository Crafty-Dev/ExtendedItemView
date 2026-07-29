package de.crafty.eiv.servercompat.util;

import com.mojang.datafixers.util.Either;
import de.crafty.eiv.servercompat.recipe.CompatRecipeManager;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.inventory.RecipeChoice;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class EivCompatTagUtil {



    private static <T> ListTag createRegistryList(List<T> objects, DefaultedRegistry<T> registry) {
        ListTag list = new ListTag();
        objects.stream().map(t -> StringTag.valueOf(registryToString(t, registry))).forEach(list::add);
        return list;
    }

    private static <T> List<T> reconstructRegistryList(CompoundTag srcTag, String key, DefaultedRegistry<T> registry) {
        return srcTag.getListOrEmpty(key).stream().map(Tag::asString).map(s -> stringToRegistry(s.orElseThrow(), registry)).filter(Objects::nonNull).toList();
    }

    /**
     * Encodes an ItemStack on the server side
     * @param stack The stack to encode
     * @return The encoded stack as CompoundTag
     */
    public static CompoundTag encodeItemStackOnServer(ItemStack stack) {
        return ItemStack.CODEC.encode(stack, CompatRecipeManager.INSTANCE.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE), new CompoundTag()).mapOrElse(tag -> tag.asCompound().orElseGet(CompoundTag::new), tagError -> new CompoundTag());
    }

    /**
     * Encodes a recipe choice
     * @param choice The recipe choice to encode
     * @return The encoded recipe choice as CompoundTag
     */
    public static CompoundTag writeChoice(RecipeChoice choice) {
        if (choice == null)
            return new CompoundTag();

        if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (org.bukkit.inventory.ItemStack stack : exactChoice.getChoices()) {
                list.add(EivCompatTagUtil.encodeItemStackOnServer(org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(stack)));
            }
            tag.put("exact_stacks", list);
            return tag;
        }

        //Convert to ingredient
        Ingredient ingredient = CraftRecipe.toIngredient(choice, false);

        if(ingredient.isEmpty())
            return new CompoundTag();

        HolderSet<Item> set = HolderSet.direct(ingredient.items().toList());

        Either<TagKey<Item>, List<Holder<Item>>> ingredientContent = set.unwrap();
        CompoundTag tag = new CompoundTag();


        if (ingredientContent.left().isPresent()) {
            tag.putString("tag", ingredientContent.left().get().location().toString());
            return tag;
        }

        if(ingredientContent.right().isEmpty())
            return new CompoundTag();

        tag.put("items", EivCompatTagUtil.createItemList(ingredientContent.right().get().stream().filter(Holder::isBound).map(Holder::value).toList()));
        return tag;
    }



    //----------------- Item, Block, Fluid -----------------

    /**
     * Pre defined method for encoding item lists
     * @param items The items to encode
     * @return The encoded item list as ListTag
     */
    public static ListTag createItemList(List<Item> items) {
        return createRegistryList(items, BuiltInRegistries.ITEM);
    }


    /**
     * Pre defined method for encoding block lists
     * @param blocks The blocks to encode
     * @return The encoded block list as ListTag
     */
    public static ListTag createBlockList(List<Block> blocks) {
        return createRegistryList(blocks, BuiltInRegistries.BLOCK);
    }
    /**
     * Pre defined method for encoding fluid lists
     * @param fluids The fluids to encode
     * @return The encoded fluid list as ListTag
     */
    public static ListTag createFluidList(List<Fluid> fluids) {
        return createRegistryList(fluids, BuiltInRegistries.FLUID);
    }

    //----------------- Custom Objects -----------------


    /**
     * Encodes a list of objects
     * @param list The list to encode
     * @param builder A CompoundBuilder defining the encoding method for a single object
     * @return The encoded list as CompoundTag
     * @param <T> The type of the list
     */
    public static <T> ListTag writeList(List<T> list, CompoundBuilder<T> builder) {
        ListTag tagList = new ListTag();
        list.stream().map(t -> builder.buildSingle(t, new CompoundTag())).forEach(tagList::add);
        return tagList;
    }


    //----------------- Registry-String Converter -----------------


    private static <T> String registryToString(T object, DefaultedRegistry<T> registry) {
        return registry.getKey(object).toString();
    }

    private static <T> T stringToRegistry(String string, DefaultedRegistry<T> registry) {
        if (string.isEmpty())
            return null;

        return registry.getOptional(Identifier.tryParse(string)).orElse(null);
    }

    /**
     * @param item The Item
     * @return The resource location of the given item as a string
     */
    public static String itemToString(Item item) {
        return registryToString(item, BuiltInRegistries.ITEM);
    }

    /**
     * @param s The resource location as a string
     * @return The item corresponding to the given resource location
     */
    public static Item itemFromString(String s) {
        return stringToRegistry(s, BuiltInRegistries.ITEM);
    }

    /**
     * @param block The Block
     * @return The resource location of the given block as a string
     */
    public static String blockToString(Block block) {
        return registryToString(block, BuiltInRegistries.BLOCK);
    }

    /**
     *
     * @param s The resource location as a string
     * @return The block corresponding to the given resource location
     */
    public static Block blockFromString(String s) {
        return stringToRegistry(s, BuiltInRegistries.BLOCK);
    }

    /**
     *
     * @param fluid The Fluid
     * @return The resource location of the given fluid as a string
     */
    public static String fluidToString(Fluid fluid) {
        return registryToString(fluid, BuiltInRegistries.FLUID);
    }

    /**
     *
     * @param s The resource location as a string
     * @return The fluid corresponding to the given resource location
     */
    public static Fluid fluidFromString(String s) {
        return stringToRegistry(s, BuiltInRegistries.FLUID);
    }

    //----------------- Custom object builder/reconstructor -----------------


    /**
     * Functional interface defining the encoding method for a single object
     * @param <T> The type of the object
     */
    public interface CompoundBuilder<T> {

        CompoundTag buildSingle(T origin, CompoundTag tag);

    }

}
