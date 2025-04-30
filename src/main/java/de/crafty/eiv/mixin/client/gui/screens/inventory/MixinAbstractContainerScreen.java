package de.crafty.eiv.mixin.client.gui.screens.inventory;

import de.crafty.eiv.ExtendedItemViewClient;
import de.crafty.eiv.overlay.ItemViewOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {


    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;


    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    protected MixinAbstractContainerScreen(Component component) {
        super(component);
    }


    @Inject(method = "init", at = @At("TAIL"))
    private void injectOverlay$0(CallbackInfo ci) {
        ItemViewOverlay.INSTANCE.initForScreen((AbstractContainerScreen<? extends AbstractContainerMenu>) (Object) this, new ItemViewOverlay.InventoryPositionInfo(this.leftPos, this.topPos, this.imageWidth, this.imageHeight));

        ItemViewOverlay.SEARCHBAR = new EditBox(font, this.width - ItemViewOverlay.INSTANCE.getWidth() / 2 - 50, this.height - 22, 100, 20, Component.literal("moin"));
        ItemViewOverlay.SEARCHBAR.setMaxLength(32);
        ItemViewOverlay.SEARCHBAR.setValue(ItemViewOverlay.INSTANCE.getCurrentQuery());
        ItemViewOverlay.SEARCHBAR.setResponder(ItemViewOverlay.INSTANCE::updateQuery);

        ItemViewOverlay.SEARCHBAR.visible = ItemViewOverlay.INSTANCE.isEnabled();
        this.addRenderableWidget(ItemViewOverlay.SEARCHBAR);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void injectOverlay$1(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (this.minecraft != null)
            ItemViewOverlay.INSTANCE.render((AbstractContainerScreen<? extends AbstractContainerMenu>) (Object) this, new ItemViewOverlay.InventoryPositionInfo(this.leftPos, this.topPos, this.imageWidth, this.imageHeight), this.minecraft, guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Inject(method = "mouseScrolled", at = @At("RETURN"))
    private void injectOverlay$2(double mouseX, double mouseY, double scrolledX, double scrolledY, CallbackInfoReturnable<Boolean> cir) {
        ItemViewOverlay.INSTANCE.scrollMouse(mouseX, mouseY, scrolledX, scrolledY);
    }


    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void injectOverlay$3(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (ItemViewOverlay.SEARCHBAR.isFocused())
            cir.setReturnValue(super.keyPressed(i, j, k));

        ItemViewOverlay.INSTANCE.keyPressed(i, j, k);

        if (this.hoveredSlot == null)
            return;

        if (ExtendedItemViewClient.USAGE_KEYBIND.matches(i, j) && this.hoveredSlot.hasItem())
            ItemViewOverlay.INSTANCE.openRecipeView(this.hoveredSlot.getItem(), ItemViewOverlay.ItemViewOpenType.INPUT);
        if (ExtendedItemViewClient.RECIPE_KEYBIND.matches(i, j) && this.hoveredSlot.hasItem())
            ItemViewOverlay.INSTANCE.openRecipeView(this.hoveredSlot.getItem(), ItemViewOverlay.ItemViewOpenType.RESULT);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void injectOverlay$3(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        if (ItemViewOverlay.SEARCHBAR.isHovered() && mouseButton == 1) {
            ItemViewOverlay.SEARCHBAR.setValue("");
            ItemViewOverlay.SEARCHBAR.setFocused(true);
            cir.setReturnValue(true);
        }

        if (mouseButton == 0 && !ItemViewOverlay.SEARCHBAR.isHovered() && ItemViewOverlay.SEARCHBAR.isFocused())
            ItemViewOverlay.SEARCHBAR.setFocused(false);

        ItemViewOverlay.INSTANCE.clickMouse((int) mouseX, (int) mouseY, mouseButton);
    }


}
