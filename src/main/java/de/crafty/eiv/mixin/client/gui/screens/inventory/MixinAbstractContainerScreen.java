package de.crafty.eiv.mixin.client.gui.screens.inventory;

import de.crafty.eiv.ExtendedItemViewClient;
import de.crafty.eiv.overlay.ItemBookmarkOverlay;
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

        this.addSearchbar(new ItemViewOverlay.InventoryPositionInfo(this.width, this.height, this.leftPos, this.topPos, this.imageWidth, this.imageHeight));

    }

    @Inject(method = "render", at = @At("HEAD"))
    private void injectOverlay$1(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (minecraft == null) return;

        ItemViewOverlay.InventoryPositionInfo info = new ItemViewOverlay.InventoryPositionInfo(this.width, this.height, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        if (ItemViewOverlay.INSTANCE.checkForScreenChange((AbstractContainerScreen<? extends AbstractContainerMenu>) (Object) this, info)) {
            if(ItemViewOverlay.SEARCHBAR != null)
                this.removeWidget(ItemViewOverlay.SEARCHBAR);

            this.addSearchbar(info);
        }
        ItemViewOverlay.INSTANCE.render((AbstractContainerScreen<? extends AbstractContainerMenu>) (Object) this, info, this.minecraft, guiGraphics, mouseX, mouseY, partialTicks);

    }

    @Unique
    private void addSearchbar(ItemViewOverlay.InventoryPositionInfo info) {
        int boxWidth = Math.min(100, info.screenWidth() - ItemViewOverlay.INSTANCE.getOverlayStartX() - 4);

        ItemViewOverlay.SEARCHBAR = new EditBox(font, this.width - ItemViewOverlay.INSTANCE.getWidth() / 2 - boxWidth / 2, this.height - 22, boxWidth, 20, Component.literal("moin"));
        ItemViewOverlay.SEARCHBAR.setMaxLength(32);
        ItemViewOverlay.SEARCHBAR.setValue(ItemViewOverlay.INSTANCE.getCurrentQuery());
        ItemViewOverlay.SEARCHBAR.setResponder(ItemViewOverlay.INSTANCE::updateQuery);

        ItemViewOverlay.SEARCHBAR.visible = ItemViewOverlay.INSTANCE.isEnabled();
        this.addRenderableWidget(ItemViewOverlay.SEARCHBAR);
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
        if(ExtendedItemViewClient.ADD_BOOKMARK_KEYBIND.matches(i, j) && this.hoveredSlot.hasItem()){
            ItemBookmarkOverlay.INSTANCE.bookmarkItem(this.hoveredSlot.getItem());

        }
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
