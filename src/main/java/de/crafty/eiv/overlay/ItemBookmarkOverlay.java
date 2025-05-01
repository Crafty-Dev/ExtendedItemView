package de.crafty.eiv.overlay;

import de.crafty.eiv.ExtendedItemViewClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ItemBookmarkOverlay {

    public static final ItemBookmarkOverlay INSTANCE = new ItemBookmarkOverlay();


    private final LinkedList<ItemSlot> slots = new LinkedList<>();

    private int width, height, xStart;
    private int fittingItemsPerRow, fittingItemsPerColumn;
    private int itemStartX, itemStartY;
    private final List<ItemStack> bookmarkedItems;

    private int startIndex;


    private ItemBookmarkOverlay() {
        this.bookmarkedItems = new ArrayList<>();
        this.startIndex = 0;
    }

    public void bookmarkItem(ItemStack stack) {
        if (!this.bookmarkedItems.contains(stack)) {
            this.bookmarkedItems.add(stack);
            this.updateSlots();
        }
    }

    public List<ItemStack> getBookmarkedItems() {
        return this.bookmarkedItems;
    }

    public void initForScreen(AbstractContainerScreen<? extends AbstractContainerMenu> screen) {
        ItemViewOverlay.InventoryPositionInfo currentInfo = ItemViewOverlay.INSTANCE.getCurrentInventoryInfo();

        if (screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen) {
            System.out.println(recipeBookScreen.recipeBookComponent.isVisible());
        }

        //-16 for Cleaner Appereance
        int spaceForOverlayX = (screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen && recipeBookScreen.recipeBookComponent.isVisible()) ? recipeBookScreen.recipeBookComponent.getXOrigin() - 32 : currentInfo.leftPos();
        spaceForOverlayX -= spaceForOverlayX > 2 * 20 + 14 ? 14 : 0;

        this.fittingItemsPerRow = Math.min(8, spaceForOverlayX / 20);
        spaceForOverlayX = this.fittingItemsPerRow * 20;


        int headlineSpace = 20;
        int bottomSpace = 40;

        int spaceForOverlayY = screen.height - (headlineSpace + bottomSpace);
        this.fittingItemsPerColumn = spaceForOverlayY / 20;

        this.width = spaceForOverlayX;
        this.height = screen.height;

        this.xStart = 0;

        this.itemStartX = 0;
        this.itemStartY = headlineSpace;

        this.updateSlots();
    }

    private void updateSlots() {
        if (this.startIndex >= this.bookmarkedItems.size())
            this.startIndex = Math.max(0, this.startIndex - this.fittingItemsPerRow * this.fittingItemsPerColumn);

        this.slots.clear();

        int endIndex = this.startIndex + (this.fittingItemsPerColumn * this.fittingItemsPerRow);

        //Slot registration
        for (int i = this.startIndex; i < endIndex && i < this.bookmarkedItems.size(); i++) {
            ItemStack stack = this.bookmarkedItems.get(i);

            int j = i - this.startIndex;

            int xOff = (j - (j / this.fittingItemsPerRow) * this.fittingItemsPerRow) * 20;
            int yOff = j / this.fittingItemsPerRow * 20;

            this.slots.add(new ItemSlot(stack, this.itemStartX + xOff + 2, this.itemStartY + yOff + 2));
        }

    }

    public void scrollMouse(double mouseX, double mouseY, double scrolledX, double scrolledY) {

        if (mouseX > this.width)
            return;

        int fittingPerPage = this.fittingItemsPerRow * this.fittingItemsPerColumn;

        if (scrolledY < 0)
            this.startIndex = Math.min(this.startIndex + fittingPerPage, this.bookmarkedItems.size() - (this.bookmarkedItems.size() - ((this.bookmarkedItems.size() - 1) / fittingPerPage) * fittingPerPage));

        if (scrolledY > 0)
            this.startIndex = Math.max(0, this.startIndex - fittingPerPage);

        if (scrolledY != 0)
            this.updateSlots();
    }

    public void clickMouse(int mouseX, int mouseY, int mouseButton) {

        if (mouseX > this.width)
            return;

        for (ItemSlot itemSlot : this.slots) {
            if (itemSlot.isHovered()) {
                itemSlot.onClicked(mouseX, mouseY, mouseButton);
                break;
            }
        }
    }

    public void keyPressed(int i, int j, int k) {

        for (ItemSlot slot : this.slots) {
            if (!slot.isHovered())
                continue;

            if (ExtendedItemViewClient.USAGE_KEYBIND.matches(i, j))
                ItemViewOverlay.INSTANCE.openRecipeView(slot.getStack(), ItemViewOverlay.ItemViewOpenType.INPUT);

            if (ExtendedItemViewClient.RECIPE_KEYBIND.matches(i, j))
                ItemViewOverlay.INSTANCE.openRecipeView(slot.getStack(), ItemViewOverlay.ItemViewOpenType.RESULT);

            if (ExtendedItemViewClient.ADD_BOOKMARK_KEYBIND.matches(i, j)) {
                this.bookmarkedItems.remove(slot.getStack());
                this.updateSlots();
            }

            break;
        }

    }

    private int getPage() {
        int fittingPerPage = this.fittingItemsPerColumn * this.fittingItemsPerRow;

        int page = this.startIndex / fittingPerPage;
        if (page * fittingPerPage < this.startIndex)
            page++;

        return page;
    }

    public void render(AbstractContainerScreen<? extends AbstractContainerMenu> screen, ItemViewOverlay.InventoryPositionInfo positionInfo, Minecraft client, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        if (this.slots.isEmpty())
            return;

        Font font = client.font;

        guiGraphics.drawCenteredString(font, Component.translatable("eiv.bookmarks").getString(), Math.max(this.width / 2, font.width(Component.translatable("eiv.bookmarks").getString()) / 2 + 2), 6, -1);
        guiGraphics.fill(this.xStart, 0, this.width, screen.height, new Color(0, 0, 0, 64).getRGB());
        String pageString = (this.getPage() + 1) + "/" + Math.max(((this.bookmarkedItems.size() - 1) / (this.fittingItemsPerColumn * this.fittingItemsPerRow) + 1), this.getPage() + 1);
        guiGraphics.drawCenteredString(font, pageString, Math.max(this.width / 2, font.width(pageString) / 2 + 2), screen.height - 2 - 20 - 10, -1);

        for (ItemSlot slot : this.slots) {
            slot.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

}
