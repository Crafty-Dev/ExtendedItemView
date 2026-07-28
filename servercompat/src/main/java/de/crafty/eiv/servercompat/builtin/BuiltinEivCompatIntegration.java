package de.crafty.eiv.servercompat.builtin;

import de.crafty.eiv.servercompat.api.CompatItemView;
import de.crafty.eiv.servercompat.api.IEivCompatIntegration;
import de.crafty.eiv.servercompat.builtin.blasting.CompatBlastingRecipe;
import de.crafty.eiv.servercompat.builtin.brewing.CompatBrewingRecipe;
import de.crafty.eiv.servercompat.builtin.burning.CompatBurningRecipe;
import de.crafty.eiv.servercompat.builtin.campfire.CompatCampfireRecipe;
import de.crafty.eiv.servercompat.builtin.shaped.CompatShapedRecipe;
import de.crafty.eiv.servercompat.builtin.shapeless.CompatShapelessRecipe;
import de.crafty.eiv.servercompat.builtin.smelting.CompatSmeltingRecipe;
import de.crafty.eiv.servercompat.builtin.smithing.CompatSmithingRecipe;
import de.crafty.eiv.servercompat.builtin.smoking.CompatSmokingRecipe;
import de.crafty.eiv.servercompat.builtin.stonecutting.CompatStonecutterRecipe;
import de.crafty.eiv.servercompat.recipe.CompatRecipeManager;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.entity.FuelValues;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.trim.CraftTrimPattern;
import org.bukkit.inventory.*;

import java.util.HashMap;

public class BuiltinEivCompatIntegration implements IEivCompatIntegration {


    @Override
    public void onInitialize() {

        //Stack Sensitives
        CompatItemView.addReloadCallback(() -> {

            Registry<Potion> potionRegistry = CompatRecipeManager.INSTANCE.getServer().registryAccess().lookupOrThrow(Registries.POTION);

            potionRegistry.forEach(potion -> {
                CompatItemView.addStackSensitive(PotionContents.createItemStack(Items.POTION, potionRegistry.wrapAsHolder(potion)));
                CompatItemView.addStackSensitive(PotionContents.createItemStack(Items.SPLASH_POTION, potionRegistry.wrapAsHolder(potion)));
                CompatItemView.addStackSensitive(PotionContents.createItemStack(Items.LINGERING_POTION, potionRegistry.wrapAsHolder(potion)));

                if (CompatRecipeManager.INSTANCE.getServer().potionBrewing().isBrewablePotion(potionRegistry.wrapAsHolder(potion))) {
                    net.minecraft.world.item.ItemStack tipped = new net.minecraft.world.item.ItemStack(Items.TIPPED_ARROW);
                    tipped.set(DataComponents.POTION_CONTENTS, new PotionContents(potionRegistry.wrapAsHolder(potion)));
                    CompatItemView.addStackSensitive(tipped);
                }
            });


            Registry<Enchantment> enchantmentRegistry = ((CraftServer)Bukkit.getServer()).getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            enchantmentRegistry.forEach(enchantment -> {
                for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) {

                    ItemStack enchantedBook = EnchantmentHelper.createBook(new EnchantmentInstance(enchantmentRegistry.wrapAsHolder(enchantment), i));
                    CompatItemView.addStackSensitive(enchantedBook);

                }
            });

        });


        //Shaped
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {

                if (recipe instanceof ShapedRecipe shapedRecipe) {

                    HashMap<Integer, RecipeChoice> choices = new HashMap<>();

                    int height = shapedRecipe.getShape().length;
                    int width = shapedRecipe.getShape()[0].length();

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {

                            char c = shapedRecipe.getShape()[y].charAt(x);
                            if (c == ' ') continue;

                            choices.put(x + y * 3, shapedRecipe.getChoiceMap().get(c));

                        }
                    }


                    recipeList.add(new CompatShapedRecipe(width, height, choices, CraftItemStack.asNMSCopy(shapedRecipe.getResult())));
                }

            });

        });

        //Shapeless
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {
                if (recipe instanceof ShapelessRecipe shapelessRecipe)
                    recipeList.add(new CompatShapelessRecipe(shapelessRecipe.getChoiceList(), CraftItemStack.asNMSCopy(shapelessRecipe.getResult())));
            });
        });

        //Smelting
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {

                if(recipe instanceof FurnaceRecipe furnaceRecipe)
                    recipeList.add(new CompatSmeltingRecipe(furnaceRecipe.getInputChoice(), CraftItemStack.asNMSCopy(furnaceRecipe.getResult())));

            });
        });

        //Blasting
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {

                if(recipe instanceof BlastingRecipe blastingRecipe)
                    recipeList.add(new CompatBlastingRecipe(blastingRecipe.getInputChoice(), CraftItemStack.asNMSCopy(blastingRecipe.getResult())));

            });
        });

        //Smoking
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {

                if(recipe instanceof SmokingRecipe smokingRecipe)
                    recipeList.add(new CompatSmokingRecipe(smokingRecipe.getInputChoice(), CraftItemStack.asNMSCopy(smokingRecipe.getResult())));

            });
        });

        //Stonecutting
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {

                if(recipe instanceof StonecuttingRecipe stonecuttingRecipe)
                    recipeList.add(new CompatStonecutterRecipe(stonecuttingRecipe.getInputChoice(), CraftItemStack.asNMSCopy(stonecuttingRecipe.getResult())));

            });

        });

        //Brewing
        CompatItemView.addRecipeProvider(recipeList -> {

            PotionBrewing potionBrewing = CompatRecipeManager.INSTANCE.getServer().potionBrewing();

            Registry<Potion> potionRegistry = CompatRecipeManager.INSTANCE.getServer().registryAccess().lookupOrThrow(Registries.POTION);

            potionRegistry.forEach(potion -> {
                ItemStack potionStack = PotionContents.createItemStack(Items.POTION, potionRegistry.wrapAsHolder(potion));
                ItemStack splashStack = PotionContents.createItemStack(Items.SPLASH_POTION, potionRegistry.wrapAsHolder(potion));
                ItemStack lingeringStack = PotionContents.createItemStack(Items.LINGERING_POTION, potionRegistry.wrapAsHolder(potion));

                BuiltInRegistries.ITEM.forEach(item -> {

                    if(potionBrewing.hasMix(potionStack, new ItemStack(item)))
                        recipeList.add(new CompatBrewingRecipe(potionBrewing.mix(new ItemStack(item), potionStack), CraftRecipe.toChoice(Ingredient.of(item)), potionStack));

                    if(potionBrewing.hasMix(splashStack, new ItemStack(item)))
                        recipeList.add(new CompatBrewingRecipe(potionBrewing.mix(new ItemStack(item), splashStack), CraftRecipe.toChoice(Ingredient.of(item)), splashStack));

                    if(potionBrewing.hasMix(lingeringStack, new ItemStack(item)))
                        recipeList.add(new CompatBrewingRecipe(potionBrewing.mix(new ItemStack(item), lingeringStack), CraftRecipe.toChoice(Ingredient.of(item)), lingeringStack));

                });
            });
        });

        //Burning
        CompatItemView.addRecipeProvider(recipeList -> {

            FuelValues fuelValues = CompatRecipeManager.INSTANCE.getServer().fuelValues();
            fuelValues.fuelItems().forEach(item -> {
                recipeList.add(new CompatBurningRecipe(item, fuelValues.burnDuration(new ItemStack(item))));
            });

        });

        //Campfire Cooking
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {

                if(recipe instanceof CampfireRecipe campfireRecipe)
                    recipeList.add(new CompatCampfireRecipe(campfireRecipe.getInputChoice(), CraftItemStack.asNMSCopy(campfireRecipe.getResult())));

            });

        });

        //Smithing
        CompatItemView.addRecipeProvider(recipeList -> {

            Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {

                if(recipe instanceof SmithingTrimRecipe trimRecipe)
                    recipeList.add(new CompatSmithingRecipe(true, trimRecipe.getBase(), trimRecipe.getTemplate(), trimRecipe.getAddition(), CraftTrimPattern.bukkitToMinecraftHolder(trimRecipe.getTrimPattern()).value(), null));

                if(recipe instanceof SmithingTransformRecipe transformRecipe)
                    recipeList.add(new CompatSmithingRecipe(false, transformRecipe.getBase(), transformRecipe.getTemplate(), transformRecipe.getAddition(), null, CraftItemStack.asNMSCopy(transformRecipe.getResult())));

            });

        });
    }


}
