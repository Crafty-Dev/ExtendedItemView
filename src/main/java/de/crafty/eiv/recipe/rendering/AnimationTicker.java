package de.crafty.eiv.recipe.rendering;

import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import net.minecraft.resources.ResourceLocation;

public class AnimationTicker {

    private final int duration;
    private int tick;
    private final ResourceLocation id;

    private AnimationTicker(ResourceLocation id, int duration) {
        this.duration = duration;
        this.id = id;
    }

    public ResourceLocation id() {
        return this.id;
    }

    public int getTick() {
        return this.tick;
    }

    public void tick() {
        this.tick++;
        if (this.tick >= this.duration)
            this.tick = 0;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public void resetTick(){
        this.tick = 0;
    }

    public float getProgress() {
        return (float) this.tick / (float) (this.duration - 1);
    }

    public int getDuration() {
        return this.duration;
    }

    public static AnimationTicker create(ResourceLocation id, int duration) {
        return new AnimationTicker(id, duration);
    }
}
