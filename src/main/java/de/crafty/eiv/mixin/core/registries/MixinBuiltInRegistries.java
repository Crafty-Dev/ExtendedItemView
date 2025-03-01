package de.crafty.eiv.mixin.core.registries;

import de.crafty.eiv.ExtendedItemView;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class MixinBuiltInRegistries {


    @Inject(method = "bootStrap", at = @At("HEAD"))
    private static void injectFluidItems(CallbackInfo ci){
        ExtendedItemView.buildFluidItems();
    }
}
