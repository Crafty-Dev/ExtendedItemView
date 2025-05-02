package de.crafty.eiv.recipe.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class EivTagUtil {


    private static <T> ListTag createRegistryList(List<T> objects, DefaultedRegistry<T> registry) {
        ListTag list = new ListTag();
        objects.stream().map(t -> StringTag.valueOf(registryToString(t, registry))).forEach(list::add);
        return list;
    }

    private static <T> List<T> reconstructRegistryList(CompoundTag srcTag, String key, DefaultedRegistry<T> registry) {
        return srcTag.getList(key).stream().map(Tag::asString).map(s -> stringToRegistry(s.orElseThrow(), registry)).toList();
    }


    public static CompoundTag encodeItemStack(ItemStack stack){
        return ItemStack.CODEC.encode(stack, NbtOps.INSTANCE, new CompoundTag()).mapOrElse(tag -> (CompoundTag) tag, tagError -> new CompoundTag());
    }

    public static ItemStack decodeItemStack(CompoundTag tag){
        return ItemStack.CODEC.decode(NbtOps.INSTANCE, tag).mapOrElse(Pair::getFirst, pairError -> ItemStack.EMPTY);
    }

    //----------------- Item, Block, Fluid -----------------

    public static ListTag createItemList(List<Item> items) {
        return createRegistryList(items, BuiltInRegistries.ITEM);
    }


    public static List<Item> reconstructItemList(CompoundTag srcTag, String key) {
        return reconstructRegistryList(srcTag, key, BuiltInRegistries.ITEM);
    }

    public static ListTag createBlockList(List<Block> items) {
        return createRegistryList(items, BuiltInRegistries.BLOCK);
    }

    public static List<Block> reconstructBlockList(CompoundTag srcTag, String key) {
        return reconstructRegistryList(srcTag, key, BuiltInRegistries.BLOCK);
    }

    public static ListTag createFluidList(List<Fluid> items) {
        return createRegistryList(items, BuiltInRegistries.FLUID);
    }


    public static List<Fluid> reconstructFluidList(CompoundTag srcTag, String key) {
        return reconstructRegistryList(srcTag, key, BuiltInRegistries.FLUID);
    }

    //----------------- Custom Objects -----------------


    public static <T> ListTag writeList(List<T> list, CompoundBuilder<T> builder) {
        ListTag tagList = new ListTag();
        list.stream().map(t -> builder.buildSingle(t, new CompoundTag())).forEach(tagList::add);
        return tagList;
    }

    public static <T> List<T> readList(CompoundTag srcTag, String key, CompoundReconstructor<T> builder) {
        return srcTag.getList(key).stream().map(Tag::asCompound).map(compoundTag -> builder.reconstructSingle(compoundTag.orElseGet(CompoundTag::new))).toList();
    }


    //----------------- Registry-String Converter -----------------


    private static <T> String registryToString(T object, DefaultedRegistry<T> registry) {
        return registry.getKey(object).toString();
    }

    private static <T> T stringToRegistry(String string, DefaultedRegistry<T> registry) {
        return registry.getValue(ResourceLocation.parse(string));
    }

    public static String itemToString(Item item) {
        return registryToString(item, BuiltInRegistries.ITEM);
    }

    public static Item itemFromString(String s) {
        return stringToRegistry(s, BuiltInRegistries.ITEM);
    }

    public static String blockToString(Block block) {
        return registryToString(block, BuiltInRegistries.BLOCK);
    }

    public static Block blockFromString(String s) {
        return stringToRegistry(s, BuiltInRegistries.BLOCK);
    }

    public static String fluidToString(Fluid fluid) {
        return registryToString(fluid, BuiltInRegistries.FLUID);
    }

    public static Fluid fluidFromString(String s) {
        return stringToRegistry(s, BuiltInRegistries.FLUID);
    }

    //----------------- Custom object builder/reconstructor -----------------


    public interface CompoundBuilder<T> {

        CompoundTag buildSingle(T origin, CompoundTag tag);

    }

    public interface CompoundReconstructor<T> {

        T reconstructSingle(CompoundTag tag);

    }
}
