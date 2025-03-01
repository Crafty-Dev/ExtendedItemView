package de.crafty.eiv;

import de.crafty.eiv.api.IExtendedItemViewIntegration;
import de.crafty.eiv.api.recipe.ItemViewRecipes;
import de.crafty.eiv.command.EivCommand;
import de.crafty.eiv.network.EivNetworkManager;
import de.crafty.eiv.network.EivNetworkServer;
import de.crafty.eiv.recipe.item.FluidItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ExtendedItemView implements ModInitializer {

    public static final String MODID = "eiv";

    public static final Logger LOGGER = LoggerFactory.getLogger("Extended ItemView");

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Minecraft!");


        EivNetworkManager.registerPayloads();
        EivNetworkServer.registerServerHandlers();

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> EivCommand.register(commandDispatcher));

        FabricLoader.getInstance().invokeEntrypoints("eiv", IExtendedItemViewIntegration.class, IExtendedItemViewIntegration::onIntegrationInitialize);

    }

    public static void buildFluidItems(){
        //Add FluidItems
        HashMap<Fluid, Item> fluidItemMap = new HashMap<>();

        BuiltInRegistries.FLUID.forEach(fluid -> {

            if (fluid == Fluids.EMPTY)
                return;
            if (!fluid.isSource(fluid.defaultFluidState()))
                return;

            ResourceLocation itemLocation = BuiltInRegistries.FLUID.getKey(fluid);
            Item item = Registry.register(
                    BuiltInRegistries.ITEM,
                    itemLocation,
                    new FluidItem(fluid.defaultFluidState().createLegacyBlock().getBlock(),
                            new FluidItem.FluidItemProperties()
                                    .fluid(fluid)
                                    .setItemId(ResourceKey.create(Registries.ITEM, itemLocation))
                    ));
            fluidItemMap.put(fluid, item);
        });

        ItemViewRecipes.INSTANCE.setFluidItemMap(fluidItemMap);
    }
}
