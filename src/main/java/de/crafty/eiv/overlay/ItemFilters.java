package de.crafty.eiv.overlay;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ItemFilters {


    protected static List<Item> defaultFilter(String query) {
        List<Item> firstPrio = new ArrayList<>();
        List<Item> secondPrio = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR)
                continue;

            String itemName = item.getName().getString().toLowerCase();

            if (itemName.startsWith(query.toLowerCase()))
                firstPrio.add(item);
            else if (itemName.contains(query.toLowerCase()))
                secondPrio.add(item);
        }

        List<Item> results = new ArrayList<>();
        results.addAll(firstPrio);
        results.addAll(secondPrio);
        return results;
    }

    protected static List<Item> modId(String query) {

        List<Item> firstPrio = new ArrayList<>();
        List<Item> secondPrio = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {

            FabricLoader.getInstance().getModContainer(BuiltInRegistries.ITEM.getKey(item).getNamespace()).ifPresent(
                    modContainer -> {
                        String modName = modContainer.getMetadata().getName().toLowerCase();
                        if(modName.startsWith(query.toLowerCase()))
                            firstPrio.add(item);
                        else if(modName.contains(query.toLowerCase()))
                            secondPrio.add(item);
                    }
            );
        }

        List<Item> results = new ArrayList<>();
        results.addAll(firstPrio);
        results.addAll(secondPrio);
        return results;
    }

    protected static List<Item> tag(String query){
        List<Item> firstPrio = new ArrayList<>();
        List<Item> secondPrio = new ArrayList<>();

        for(Item item : BuiltInRegistries.ITEM){

        }

        List<Item> results = new ArrayList<>();
        results.addAll(firstPrio);
        results.addAll(secondPrio);

        return results;
    }
}
