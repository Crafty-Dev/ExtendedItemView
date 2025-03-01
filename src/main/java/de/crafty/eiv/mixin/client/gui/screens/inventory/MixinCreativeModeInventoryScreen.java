package de.crafty.eiv.mixin.client.gui.screens.inventory;

import de.crafty.eiv.overlay.ItemViewOverlay;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements FabricCreativeInventoryScreen {


    public MixinCreativeModeInventoryScreen(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void injectSearchBar(char c, int i, CallbackInfoReturnable<Boolean> cir){
        if(ItemViewOverlay.SEARCHBAR.isFocused())
            cir.setReturnValue(super.charTyped(c, i));
    }
}
