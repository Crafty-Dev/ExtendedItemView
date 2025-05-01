package de.crafty.eiv.overlay;

import de.crafty.eiv.ExtendedItemViewClient;
import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.recipe.ClientRecipeCache;
import de.crafty.eiv.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ItemViewOverlay {

    public static final ItemViewOverlay INSTANCE = new ItemViewOverlay();


    private final LinkedList<ItemSlot> slots = new LinkedList<>();

    public static EditBox SEARCHBAR = null;

    private int width, height, xStart;
    private int fittingItemsPerRow, fittingItemsPerColumn;
    private int itemStartX, itemStartY;
    private String currentQuery;
    private List<Item> availableItems;

    private int startIndex;

    private boolean enabled;
    private InventoryPositionInfo currentInfo;

    private ItemViewOverlay() {
        this.currentQuery = "";
        this.availableItems = new ArrayList<>();
        this.startIndex = 0;
        this.enabled = true;

        this.currentInfo = null;

    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean prev = this.enabled;
        this.enabled = enabled;

        if (prev != enabled && enabled)
            ItemViewOverlay.SEARCHBAR.visible = true;

        if (prev != enabled && !enabled)
            ItemViewOverlay.SEARCHBAR.visible = false;
    }

    public InventoryPositionInfo getCurrentInventoryInfo() {
        return this.currentInfo;
    }

    public int getFittingItemsPerColumn() {
        return this.fittingItemsPerColumn;
    }

    public int getFittingItemsPerRow() {
        return this.fittingItemsPerRow;
    }

    public String getCurrentQuery() {
        return this.currentQuery;
    }

    public int getOverlayStartX() {
        return this.xStart;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public List<Item> getAvailableItems() {
        return this.availableItems;
    }


    public boolean checkForScreenChange(AbstractContainerScreen<? extends AbstractContainerMenu> screen, InventoryPositionInfo newInfo) {
        if (!newInfo.matches(this.currentInfo)) {
            this.currentInfo = newInfo;
            this.initForScreen(screen);
            ItemBookmarkOverlay.INSTANCE.initForScreen(screen);
            return true;
        }

        return false;
    }

    public void initForScreen(AbstractContainerScreen<? extends AbstractContainerMenu> screen) {
        //-16 for Cleaner Appereance
        int spaceForOverlayX = screen.width - (this.currentInfo.leftPos + this.currentInfo.imageWidth);
        spaceForOverlayX -= spaceForOverlayX > 2 * 20 + 14 ? 14 : 0;

        this.fittingItemsPerRow = Math.min(8, spaceForOverlayX / 20);
        spaceForOverlayX = this.fittingItemsPerRow * 20;


        int headlineSpace = 20;
        int bottomSpace = 40;

        int spaceForOverlayY = screen.height - (headlineSpace + bottomSpace);
        this.fittingItemsPerColumn = spaceForOverlayY / 20;

        this.width = spaceForOverlayX;
        this.height = screen.height;

        this.xStart = screen.width - this.width;

        this.itemStartX = this.xStart;
        this.itemStartY = headlineSpace;

        this.updateQuery(this.getCurrentQuery());
    }

    private void updateSlots() {
        this.slots.clear();

        int endIndex = this.startIndex + (this.fittingItemsPerColumn * this.fittingItemsPerRow);


        //Slot registration
        for (int i = this.startIndex; i < endIndex && i < this.availableItems.size(); i++) {
            Item item = this.availableItems.get(i);

            int j = i - this.startIndex;

            int xOff = (j - (j / this.fittingItemsPerRow) * this.fittingItemsPerRow) * 20;
            int yOff = j / this.fittingItemsPerRow * 20;

            this.slots.add(new ItemSlot(new ItemStack(item), this.itemStartX + xOff, this.itemStartY + yOff));
        }

    }

    public void updateQuery(String newQuery) {
        if (!newQuery.equals(this.currentQuery))
            this.startIndex = 0;

        this.currentQuery = newQuery;

        if (newQuery.startsWith("@"))
            this.availableItems = ItemFilters.modId(newQuery.substring(1));
        else
            this.availableItems = ItemFilters.defaultFilter(newQuery);

        this.updateSlots();
    }

    public void scrollMouse(double mouseX, double mouseY, double scrolledX, double scrolledY) {

        if (!this.isEnabled())
            return;

        ItemBookmarkOverlay.INSTANCE.scrollMouse(mouseX, mouseY, scrolledX, scrolledY);

        if (mouseX < this.itemStartX)
            return;

        int fittingPerPage = this.fittingItemsPerRow * this.fittingItemsPerColumn;

        if (scrolledY < 0)
            this.startIndex = Math.min(this.startIndex + fittingPerPage, this.availableItems.size() - (this.availableItems.size() - (this.availableItems.size() / fittingPerPage) * fittingPerPage));

        if (scrolledY > 0)
            this.startIndex = Math.max(0, this.startIndex - fittingPerPage);

        if (scrolledY != 0)
            this.updateSlots();
    }

    public void clickMouse(int mouseX, int mouseY, int mouseButton) {
        if (!this.isEnabled())
            return;

        ItemBookmarkOverlay.INSTANCE.clickMouse(mouseX, mouseY, mouseButton);

        if (mouseX < this.xStart)
            return;

        for (ItemSlot itemSlot : this.slots) {
            if (itemSlot.isHovered()) {
                itemSlot.onClicked(mouseX, mouseY, mouseButton);
                break;
            }
        }
    }

    public void keyPressed(int i, int j, int k) {

        if (!ItemViewOverlay.SEARCHBAR.isFocused() && ExtendedItemViewClient.TOGGLE_OVERLAY_KEYBIND.matches(i, j))
            ItemViewOverlay.INSTANCE.setEnabled(!ItemViewOverlay.INSTANCE.isEnabled());

        if (!this.isEnabled())
            return;

        ItemBookmarkOverlay.INSTANCE.keyPressed(i, j, k);

        for (ItemSlot slot : this.slots) {
            if (!slot.isHovered())
                continue;

            if (ExtendedItemViewClient.USAGE_KEYBIND.matches(i, j))
                ItemViewOverlay.INSTANCE.openRecipeView(slot.getStack(), ItemViewOverlay.ItemViewOpenType.INPUT);

            if (ExtendedItemViewClient.RECIPE_KEYBIND.matches(i, j))
                ItemViewOverlay.INSTANCE.openRecipeView(slot.getStack(), ItemViewOverlay.ItemViewOpenType.RESULT);

            if(ExtendedItemViewClient.ADD_BOOKMARK_KEYBIND.matches(i, j))
                ItemBookmarkOverlay.INSTANCE.bookmarkItem(slot.getStack());

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


    public void render(AbstractContainerScreen<? extends AbstractContainerMenu> screen, InventoryPositionInfo positionInfo, Minecraft client, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        if (!this.isEnabled())
            return;

        ItemBookmarkOverlay.INSTANCE.render(screen, positionInfo, client, guiGraphics, mouseX, mouseY, partialTicks);


        Font font = client.font;

        guiGraphics.drawCenteredString(font, "ItemView", screen.width - this.getWidth() / 2, 6, -1);
        guiGraphics.fill(this.xStart, 0, screen.width, screen.height, new Color(0, 0, 0, 64).getRGB());
        guiGraphics.drawCenteredString(font, (this.getPage() + 1) + "/" + (this.availableItems.size() / (this.fittingItemsPerColumn * this.fittingItemsPerRow) + 1), screen.width - this.width / 2, screen.height - 2 - 20 - 10, -1);

        for (ItemSlot slot : this.slots) {
            slot.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

    }


    public void openRecipeView(ItemStack stack, ItemViewOpenType openType) {
        if (stack.isEmpty())
            return;

        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null)
            return;

        List<IEivViewRecipe> foundRecipes = openType.recipeProvider().retrieveRecipes(stack);

        if (!foundRecipes.isEmpty()) {
            Screen parent = Minecraft.getInstance().screen;
            if (parent instanceof RecipeViewScreen viewScreen)
                parent = viewScreen.getMenu().getParentScreen();

            Minecraft.getInstance().setScreen(new RecipeViewScreen(new RecipeViewMenu(parent, 0, clientPlayer.getInventory(), foundRecipes, stack), clientPlayer.getInventory(), Component.empty()));
        }


    }

    public enum ItemViewOpenType {
        INPUT(ClientRecipeCache.INSTANCE::getRecipesForCraftingInput),
        RESULT(ClientRecipeCache.INSTANCE::getRecipesForCraftingOutput);

        final RecipeProvider recipeProvider;

        ItemViewOpenType(RecipeProvider recipeProvider) {
            this.recipeProvider = recipeProvider;
        }

        RecipeProvider recipeProvider() {
            return this.recipeProvider;
        }

        interface RecipeProvider {

            List<IEivViewRecipe> retrieveRecipes(ItemStack stack);
        }
    }

    public record InventoryPositionInfo(int screenWidth, int screenHeight, int leftPos, int topPos, int imageWidth,
                                        int imageHeight) {


        private boolean matches(@Nullable InventoryPositionInfo info) {
            if (info == null)
                return false;

            return info.screenWidth == this.screenWidth
                    && info.screenHeight == this.screenHeight
                    && info.leftPos == this.leftPos
                    && info.topPos == this.topPos
                    && info.imageWidth == this.imageWidth
                    && info.imageHeight == this.imageHeight;
        }

    }
}
