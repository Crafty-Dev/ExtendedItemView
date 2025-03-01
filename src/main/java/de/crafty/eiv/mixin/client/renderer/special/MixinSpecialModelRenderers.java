package de.crafty.eiv.mixin.client.renderer.special;

import com.mojang.serialization.MapCodec;
import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.extra.FluidItemSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialModelRenderers.class)
public abstract class MixinSpecialModelRenderers {

    @Shadow
    @Final
    public static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends SpecialModelRenderer.Unbaked>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("HEAD"))
    private static void injectFluidItemRenderer(CallbackInfo ci){
        ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "fluiditem"), FluidItemSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
