package de.crafty.eiv.recipe;

import de.crafty.eiv.network.payload.ServerboundRequestRecipesPayload;
import de.crafty.eiv.recipe.cache.ModRecipeCache;
import de.crafty.eiv.recipe.cache.VanillaRecipeCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class ClientRecipeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("ClientRecipeManager");

    public static final ClientRecipeManager INSTANCE = new ClientRecipeManager();

    private volatile Status status;

    private ClientRecipeManager() {
        this.status = new Status("EIV - ", 20 * 60 * 5);
    }

    public Status status() {
        return this.status;
    }


    public void startUpdate() {
        if(!this.status().isIdle())
            return;

        this.status().setIdle(false);
        this.status().setUpdateStartTimestamp();

        new Thread(() -> {

            while (!this.status().isIdle()) {
                //Cleanup on timeout
                if (this.status().networkTimeout()) {
                    this.status().setIdle(true);
                    VanillaRecipeCache.INSTANCE.clear();
                    ModRecipeCache.INSTANCE.clear();
                    return;
                }
            }

        }, "EIV-Network-Timeout-Handler Thread").start();
    }

    public void processRecipes() {

        new Thread(() -> {

            this.status.setStatusStep("Processing Recipes");
            this.status.setStatusProgress("0%");

            boolean vanillaSuccess = VanillaRecipeCache.INSTANCE.processRecipes();
            boolean modSuccess = ModRecipeCache.INSTANCE.processRecipes();

            VanillaRecipeCache.INSTANCE.clear();
            ModRecipeCache.INSTANCE.clear();

            if (!(vanillaSuccess && modSuccess))
                LOGGER.error("Something went wrong while processing recipes, there might be some strange appearances");

            this.status.setIdle(true);

        }, "EIV-Process-Recipe-Cache Thread").start();

    }

    public void requestRecipesFromServer() {
        //TODO only send when not caching
        if(this.status.isIdle())
            ClientPlayNetworking.send(new ServerboundRequestRecipesPayload());

    }


    public static class Status {

        final String prefix;
        String statusStep, statusProgress;
        boolean idle;
        long updateStartTimestamp, networkTimeout;

        Status(String prefix, long networkTimeout) {
            this.prefix = prefix;
            this.statusStep = "";
            this.statusProgress = "";
            this.idle = true;

            this.updateStartTimestamp = -1;
            this.networkTimeout = networkTimeout;
        }

        public void setIdle(boolean idle) {
            this.idle = idle;
            if(idle)
                this.updateStartTimestamp = -1;
        }

        public boolean isIdle() {
            return this.idle;
        }

        public void setUpdateStartTimestamp() {
            if (Minecraft.getInstance().level != null)
                this.updateStartTimestamp = Minecraft.getInstance().level.getGameTime();
        }

        public boolean networkTimeout() {
            if (Minecraft.getInstance().level == null)
                return true;

            return Minecraft.getInstance().level.getGameTime() - this.updateStartTimestamp > this.networkTimeout;
        }

        public void setStatusStep(String statusStep) {
            this.statusStep = statusStep;
        }

        public void setStatusProgress(String statusProgress) {
            this.statusProgress = statusProgress;
        }


        public String get() {
            return this.prefix + this.statusStep + ": " + this.statusProgress;
        }
    }
}
