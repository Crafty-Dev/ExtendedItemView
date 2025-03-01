package de.crafty.eiv.recipe.inventory;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.ExtendedItemViewClient;
import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class RecipeViewMenu extends AbstractContainerMenu {

    //For screen and space calculation
    protected static final int MAX_POSSIBLE_HEIGHT = 224;
    protected static final int BUFFER_ZONE = 16;
    protected static final int TOP_SPACE = 24;
    protected static final int BOTTOM_SPACE = 24;

    private final Player player;
    private ViewContainer viewContainer;

    private List<? extends IEivViewRecipe> recipes;
    private IEivRecipeViewType viewType;

    private int maxPossiblePerPage;
    private int maxPageIndex;
    private int currentPage;

    private final List<IEivViewRecipe> currentDisplay;

    private final LinkedHashMap<IEivRecipeViewType, List<IEivViewRecipe>> sortedByType;
    private final List<IEivRecipeViewType> viewTypeOrder;
    private int currentTypeIndex;

    private int menuWidth, menuHeight;
    private final ItemStack origin;
    private final HashMap<Integer, AdditionalStackModifier> additionalStackModifiers;

    private RecipeViewScreen viewScreen;
    private final Screen parentScreen;

    public RecipeViewMenu(Screen parentScreen, int containerId, Inventory inventory, List<? extends IEivViewRecipe> recipes, ItemStack origin) {
        super(ExtendedItemViewClient.RECIPE_VIEW_MENU, containerId);

        this.parentScreen = parentScreen;

        this.origin = origin;
        this.additionalStackModifiers = new HashMap<>();

        this.sortedByType = new LinkedHashMap<>();
        HashMap<IEivRecipeViewType, HashMap<Integer, List<IEivViewRecipe>>> prioOrder = new HashMap<>();

        recipes.forEach(iEivRecipe -> {
            List<IEivViewRecipe> list = prioOrder.getOrDefault(iEivRecipe.getViewType(), new HashMap<>()).getOrDefault(iEivRecipe.getPriority(), new ArrayList<>());
            list.add(iEivRecipe);
            HashMap<Integer, List<IEivViewRecipe>> map = prioOrder.getOrDefault(iEivRecipe.getViewType(), new HashMap<>());
            map.put(iEivRecipe.getPriority(), list);
            prioOrder.put(iEivRecipe.getViewType(), map);
        });

        prioOrder.forEach((viewType, map) -> {
            List<IEivViewRecipe> list = new ArrayList<>();
            map.values().forEach(list::addAll);
            this.sortedByType.put(viewType, list);
        });

        this.viewTypeOrder = new ArrayList<>();
        this.sortedByType.forEach((viewType, iEivRecipes) -> {
            this.viewTypeOrder.add(viewType);
        });

        this.currentTypeIndex = 0;

        this.currentPage = 0;
        this.currentDisplay = new ArrayList<>();


        if (recipes.isEmpty())
            ExtendedItemView.LOGGER.error("Attempting to open Menu with 0 recipes");

        player = inventory.player;
        this.updateByViewType();

        if (!this.sortedByType.isEmpty())
            return;

        this.viewContainer = new ViewContainer(0);
        this.viewType = IEivRecipeViewType.NONE;

    }

    public RecipeViewMenu(int containerId, Inventory inventory) {
        this(null, containerId, inventory, IEivViewRecipe.PLACEHOLDER, ItemStack.EMPTY);
    }

    public Screen getParentScreen() {
        return this.parentScreen;
    }

    public void setViewScreen(RecipeViewScreen viewScreen) {
        this.viewScreen = viewScreen;
    }

    public ItemStack getOrigin() {
        return this.origin;
    }

    public AdditionalStackModifier getAdditionalStackModifier(int slot) {
        return this.additionalStackModifiers.getOrDefault(slot, AdditionalStackModifier.NONE);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.viewContainer.stillValid(player);
    }


    public int getMaxPossiblePerPage() {
        return this.maxPossiblePerPage;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public int getMaxPageIndex() {
        return this.maxPageIndex;
    }

    public void nextPage() {
        this.currentPage = Math.min(this.currentPage + 1, this.maxPageIndex);
        this.updateByPage();
    }

    public void prevPage() {
        this.currentPage = Math.max(this.currentPage - 1, 0);
        this.updateByPage();
    }

    public void nextType() {
        int prevIndex = this.currentTypeIndex;
        this.currentTypeIndex = Math.min(this.currentTypeIndex + 1, this.viewTypeOrder.size() - 1);

        if (prevIndex != this.currentTypeIndex)
            this.updateByViewType();

    }

    public void prevType() {
        int prevIndex = this.currentTypeIndex;
        this.currentTypeIndex = Math.max(this.currentTypeIndex - 1, 0);

        if (prevIndex != this.currentTypeIndex)
            this.updateByViewType();

    }

    public boolean hasNextType() {
        return this.currentTypeIndex < this.viewTypeOrder.size() - 1;
    }

    public boolean hasPrevType() {
        return this.currentTypeIndex > 0;
    }

    protected List<IEivViewRecipe> getCurrentDisplay() {
        return this.currentDisplay;
    }

    private List<IEivViewRecipe> getRecipeDisplay() {
        List<IEivViewRecipe> recipesOnPage = new ArrayList<>();
        for (int i = this.currentPage * this.maxPossiblePerPage; i < Math.min(this.getRecipes().size(), (this.currentPage + 1) * this.maxPossiblePerPage); i++) {
            recipesOnPage.add(this.getRecipes().get(i));
        }

        return recipesOnPage;
    }

    protected void updateByPage() {
        this.additionalStackModifiers.clear();

        this.slots.clear();
        this.currentDisplay.clear();

        this.currentDisplay.addAll(this.getRecipeDisplay());

        for (int i = 0; i < this.currentDisplay.size(); i++) {

            IEivViewRecipe recipe = this.currentDisplay.get(i);
            recipe.getIngredients().forEach(slotContent -> slotContent.bindOrigin(this.origin));
            recipe.getResults().forEach(slotContent -> slotContent.bindOrigin(this.origin));

            SlotDefinition slotDefinition = new SlotDefinition();
            this.viewType.placeSlots(slotDefinition);
            for (Slot slot : slotDefinition.getItemSlots()) {
                int id = slot.getContainerSlot() + (i * this.getViewType().getSlotCount());

                this.addSlot(new Slot(slot.container, id, slot.x + this.guiOffsetLeft(), slot.y + this.guiOffsetTop(i)));
            }

            SlotFillContext slotFillContext = new SlotFillContext();
            recipe.bindSlots(slotFillContext);

            for (int j = 0; j < this.getViewType().getSlotCount(); j++) {
                int slotId = j + (i * this.getViewType().getSlotCount());
                this.viewContainer.setItem(slotId, slotFillContext.contentBySlot(j).getByIndex(slotFillContext.contentBySlot(j).index()));

                if (slotFillContext.getAdditionalTooltips().containsKey(j))
                    this.additionalStackModifiers.put(slotId, slotFillContext.getAdditionalTooltips().get(j));

            }


        }
    }

    private void resetContentPointers() {
        this.recipes.forEach(iEivRecipe -> {
            iEivRecipe.getIngredients().forEach(SlotContent::resetPointer);
            iEivRecipe.getResults().forEach(SlotContent::resetPointer);
        });
    }

    protected void updateByViewType() {

        this.currentPage = 0;
        this.recipes = this.sortedByType.getOrDefault(this.viewTypeOrder.get(this.currentTypeIndex), new ArrayList<>());
        this.resetContentPointers();

        Optional<? extends IEivViewRecipe> optional = recipes.stream().findFirst();

        if (optional.isPresent()) {
            this.viewType = optional.get().getViewType();
            this.maxPossiblePerPage = this.calculateRecipesPerPage();

            int i = this.getRecipes().size() / this.maxPossiblePerPage;
            if (this.getRecipes().size() % this.maxPossiblePerPage != 0)
                i++;

            this.maxPageIndex = i - 1;

            this.viewContainer = new ViewContainer(this.viewType.getSlotCount() * this.maxPossiblePerPage);

            this.setMenuSizes();
            this.updateByPage();

        }
    }

    private void setMenuSizes() {
        this.menuHeight = TOP_SPACE + this.getRecipeDisplay().size() * this.getViewType().getDisplayHeight() + (this.getRecipeDisplay().size() * RecipeViewMenu.BUFFER_ZONE) + (BOTTOM_SPACE - BUFFER_ZONE);

        this.menuWidth = 176;
    }

    public int getHeight() {
        return this.menuHeight;
    }

    public int getWidth() {
        return this.menuWidth;
    }

    //Returns how far the viewtype-specific texture is away from the border
    protected int guiOffsetLeft() {
        return (this.menuWidth - this.getViewType().getDisplayWidth()) / 2;
    }

    protected int guiOffsetTop(int displayIndex) {
        return TOP_SPACE + (displayIndex * (this.getViewType().getDisplayHeight() + BUFFER_ZONE));
    }

    protected void tickContents() {

        for (int i = 0; i < this.currentDisplay.size(); i++) {
            IEivViewRecipe recipe = this.currentDisplay.get(i);

            SlotFillContext slotFillContext = new SlotFillContext();
            recipe.bindSlots(slotFillContext);

            for (int j = 0; j < this.getViewType().getSlotCount(); j++) {

                //Exclude DependencySlots
                if(!slotFillContext.contentDependencies.containsKey(j))
                    this.viewContainer.setItem(j + (i * this.getViewType().getSlotCount()), slotFillContext.contentBySlot(j).next());
            }
            for (int j = 0; j < this.getViewType().getSlotCount(); j++) {

                //Exclude DependencySlots
                if(slotFillContext.contentDependencies.containsKey(j))
                    this.viewContainer.setItem(j + (i * this.getViewType().getSlotCount()), slotFillContext.contentBySlot(j).getByIndex(slotFillContext.contentDependencies.get(j).get()));
            }
        }
    }

    public List<? extends IEivViewRecipe> getRecipes() {
        return this.recipes;
    }

    public IEivRecipeViewType getViewType() {
        return this.viewType;
    }

    public ViewContainer getViewContainer() {
        return this.viewContainer;
    }


    private int calculateRecipesPerPage() {
        if (this.getRecipes().isEmpty())
            return 0;

        int recipeHeight = this.getViewType().getDisplayHeight();

        int technicallyFitting = Math.min(this.getRecipes().size(), MAX_POSSIBLE_HEIGHT / recipeHeight);
        int imageheightRequired = (technicallyFitting * recipeHeight) + (technicallyFitting * BUFFER_ZONE + TOP_SPACE + BOTTOM_SPACE);

        while (imageheightRequired > MAX_POSSIBLE_HEIGHT) {
            technicallyFitting -= 1;

            imageheightRequired = (technicallyFitting * recipeHeight) + (technicallyFitting * BUFFER_ZONE + TOP_SPACE + BOTTOM_SPACE);
        }

        return technicallyFitting;
    }


    public class SlotDefinition {

        private final HashMap<Integer, Slot> itemSlots;

        private SlotDefinition() {
            this.itemSlots = new HashMap<>();
        }

        public void addItemSlot(int slotId, int x, int y) {
            this.itemSlots.put(slotId, new Slot(RecipeViewMenu.this.viewContainer, slotId, x, y));
        }

        private List<Slot> getItemSlots() {
            return this.itemSlots.values().stream().toList();
        }
    }

    public static class SlotFillContext {

        private final HashMap<Integer, SlotContent> contents;
        private final HashMap<Integer, Supplier<Integer>> contentDependencies;
        private final HashMap<Integer, AdditionalStackModifier> additionalTooltips;

        protected SlotFillContext() {
            this.contents = new HashMap<>();
            this.contentDependencies = new HashMap<>();

            this.additionalTooltips = new HashMap<>();
        }

        public void bindSlot(int slotId, SlotContent slotContent) {
            this.contents.put(slotId, slotContent);
        }

        public void bindDepedantSlot(int slotId, Supplier<Integer> dependantIndex, SlotContent slotContent){
            this.contents.put(slotId, slotContent);
            this.contentDependencies.put(slotId, dependantIndex);
        }

        public HashMap<Integer, Supplier<Integer>> contentDependencies() {
            return this.contentDependencies;
        }

        public void addAdditionalStackModifier(int slotId, AdditionalStackModifier tooltipProvider) {
            this.additionalTooltips.put(slotId, tooltipProvider);
        }

        private HashMap<Integer, SlotContent> getContents() {
            return this.contents;
        }

        protected SlotContent contentBySlot(int slotId){
            return this.contents.getOrDefault(slotId, SlotContent.of(List.of()));
        }

        private HashMap<Integer, AdditionalStackModifier> getAdditionalTooltips() {
            return this.additionalTooltips;
        }
    }

    public static class SlotContentMap {

        private HashMap<Integer, SlotContent> mappedContents;

        private SlotContentMap() {
            this.mappedContents = new HashMap<>();
        }

        private void mapIngredient(int containerSlot, SlotContent ingredient) {
            this.mappedContents.put(containerSlot, ingredient);
        }

    }

    public interface AdditionalStackModifier {

        AdditionalStackModifier NONE = (stack, tooltip) -> {
        };

        void addTooltip(ItemStack stack, List<Component> tooltip);

    }
}
