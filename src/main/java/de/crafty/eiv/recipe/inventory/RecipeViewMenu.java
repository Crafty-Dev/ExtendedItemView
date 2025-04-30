package de.crafty.eiv.recipe.inventory;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.ExtendedItemViewClient;
import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
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

    private final List<RecipeTransferData> transferData;


    public RecipeViewMenu(Screen parentScreen, int containerId, Inventory inventory, List<? extends IEivViewRecipe> recipes, ItemStack origin) {
        super(ExtendedItemViewClient.RECIPE_VIEW_MENU, containerId);

        this.parentScreen = parentScreen;
        this.transferData = new ArrayList<>();

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

        //Sorting recipe types
        this.viewTypeOrder = new ArrayList<>();
        List<IEivRecipeViewType> unsortedTypes = this.sortedByType.keySet().stream().toList();
        HashMap<String, IEivRecipeViewType> byId = new HashMap<>();
        unsortedTypes.forEach(viewType -> {
            byId.put(viewType.getId().toString(), viewType);
        });

        List<String> ids = new ArrayList<>(byId.keySet());
        ids.sort(String::compareTo);

        ids.forEach(id -> {
           this.viewTypeOrder.add(byId.get(id));
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

    public void nextRecipe() {
        int prevPage = this.currentPage;
        this.currentPage = Math.min(this.currentPage + 1, this.maxPageIndex);

        if (prevPage != this.currentPage)
            this.updateByPage();

    }

    public void setViewType(int typeId) {
        int prevIndex = this.currentTypeIndex;
        this.currentTypeIndex = typeId;

        if (prevIndex != this.currentTypeIndex)
            this.updateByViewType();
    }

    public boolean hasNextRecipe() {
        return this.currentPage < this.maxPageIndex;
    }

    public boolean hasPrevRecipe() {
        return this.currentPage > 0;
    }

    public List<IEivRecipeViewType> getViewTypeOrder() {
        return this.viewTypeOrder;
    }

    public int getCurrentTypeIndex() {
        return this.currentTypeIndex;
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

            recipe.getIngredients().forEach(slotContent -> slotContent.setType(SlotContent.Type.INGREDIENT));
            recipe.getResults().forEach(slotContent -> slotContent.setType(SlotContent.Type.RESULT));

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


        this.transferData.clear();
        for (int i = 0; i < this.getCurrentDisplay().size(); i++) {
            this.transferData.add(this.checkMatchingContent(i));
        }

        if (this.viewScreen != null)
            this.viewScreen.checkGui();


        this.updateDependencies();
    }

    public List<RecipeTransferData> getTransferData() {
        return this.transferData;
    }

    private RecipeTransferData checkMatchingContent(int displayId) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null)
            return RecipeTransferData.EMPTY;

        IEivViewRecipe currentLooking = this.getCurrentDisplay().get(displayId);

        RecipeViewMenu.SlotFillContext context = new RecipeViewMenu.SlotFillContext();
        currentLooking.bindSlots(context);

        LocalPlayer player = minecraft.player;

        NonNullList<ItemStack> playerInvCache = NonNullList.withSize(player.getInventory().items.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < playerInvCache.size(); slot++)
            playerInvCache.set(slot, player.getInventory().getItem(slot).copy());


        RecipeTransferData.Builder dataBuilder = new RecipeTransferData.Builder();
        context.getContents().keySet().stream().filter(slot -> context.getContents().get(slot).getType() != SlotContent.Type.RESULT).forEach(dataBuilder::noticeSlot);

        HashMap<Integer, List<ItemStack>> validAndAvailableContent = new HashMap<>();

        //Filter relevant items to check and exclude air + results
        for (int slot : context.getContents().keySet()) {

            SlotContent content = context.getContents().get(slot);

            if (content.getType() == SlotContent.Type.RESULT)
                continue;

            if (content.isEmpty()) {
                dataBuilder.findContent(slot, new HashMap<>());
                continue;
            }

            List<ItemStack> availableItems = new ArrayList<>();
            content.getValidContents().forEach(stack -> {
                if (playerInvCache.stream().anyMatch(stack1 -> stack1.is(stack.getItem())))
                    availableItems.add(stack);
            });

            validAndAvailableContent.put(slot, availableItems);
        }


        List<Integer> slots = new ArrayList<>();
        context.getContents().forEach((slot, slotContent) -> {
            if (slotContent.getType() != SlotContent.Type.RESULT)
                slots.add(slot);
        });


        HashMap<Integer, HashMap<Integer, ItemStack>> bestMatch = new HashMap<>();
        this.check(slots, 0, validAndAvailableContent, new HashMap<>(), bestMatch, playerInvCache);
        bestMatch.forEach(dataBuilder::findContent);

        RecipeTransferData transferData = dataBuilder.build();

        if (transferData.isSuccess()) {

            HashMap<Integer, ItemStack> requiredStacks = new HashMap<>();
            for (int recipeSlot : transferData.getUsedPlayerSlots().keySet()) {

                HashMap<Integer, ItemStack> usedSlots = transferData.getUsedPlayerSlots().get(recipeSlot);

                ItemStack required = usedSlots.values().stream().findFirst().orElseGet(() -> ItemStack.EMPTY).copy();

                int amount = 0;
                for (ItemStack stack : usedSlots.values()) {
                    amount += stack.getCount();
                }

                if (!required.isEmpty())
                    requiredStacks.put(recipeSlot, required.copyWithCount(amount));
            }

            boolean checking = true;
            HashMap<Integer, HashMap<Integer, ItemStack>> stackable = new HashMap<>();

            int runs = 0;

            while (checking) {

                for (int recipeSlot : requiredStacks.keySet()) {

                    if ((runs + 1) * requiredStacks.get(recipeSlot).getCount() > requiredStacks.get(recipeSlot).getMaxStackSize() - requiredStacks.get(recipeSlot).getCount()) {
                        checking = false;
                        break;
                    }
                }

                if (!checking)
                    break;

                HashMap<Integer, HashMap<Integer, ItemStack>> currentStackable = new HashMap<>();

                for (int recipeSlot : requiredStacks.keySet()) {

                    ItemStack requiredStack = requiredStacks.get(recipeSlot);

                    HashMap<Integer, ItemStack> found = this.invCheckAndFind(playerInvCache, requiredStack, true);
                    if (found.isEmpty()) {
                        checking = false;
                        break;
                    }

                    currentStackable.put(recipeSlot, found);
                }

                if (checking) {
                    //Add to complete map
                    currentStackable.forEach((recipeSlot, usedPlayerSlots) -> {

                        HashMap<Integer, ItemStack> presentStackable = stackable.getOrDefault(recipeSlot, new HashMap<>());

                        usedPlayerSlots.forEach((playerSlot, stack) -> {
                            if (presentStackable.containsKey(playerSlot))
                                presentStackable.put(playerSlot, stack.copyWithCount(presentStackable.get(playerSlot).getCount() + stack.getCount()));
                            else
                                presentStackable.put(playerSlot, stack);
                        });

                        stackable.put(recipeSlot, presentStackable);
                    });

                    runs++;
                }
            }


            HashMap<Integer, HashMap<Integer, ItemStack>> stackedMatch = new HashMap<>();
            bestMatch.forEach((recipeSlot, usedPlayerSlots) -> {

                HashMap<Integer, ItemStack> playerSlots = new HashMap<>();

                usedPlayerSlots.forEach((playerSlot, stack) -> {
                    playerSlots.put(playerSlot, stack.copy());
                });
                stackedMatch.put(recipeSlot, playerSlots);

            });

            stackable.forEach((recipeSlot, usedPlayerSlots) -> {

                usedPlayerSlots.forEach((playerSlot, stack) -> {

                    if (stackedMatch.get(recipeSlot).containsKey(playerSlot))
                        stackedMatch.get(recipeSlot).put(playerSlot, stack.copyWithCount(stackedMatch.get(recipeSlot).get(playerSlot).getCount() + stack.getCount()));
                    else
                        stackedMatch.get(recipeSlot).put(playerSlot, stack);

                });

            });

            RecipeTransferData.Builder stackedBuilder = dataBuilder.duplicate();

            stackedMatch.forEach(stackedBuilder::findContent);
            transferData.setStackedData(stackedBuilder.build());
        }

        return transferData;
    }


    private void check(List<Integer> slots, int currentSlotIndex, HashMap<Integer, List<ItemStack>> validAndAvailableContent, HashMap<Integer, HashMap<Integer, ItemStack>> usedPlayerSlots, HashMap<Integer, HashMap<Integer, ItemStack>> bestMatch, NonNullList<ItemStack> playerInvCache) {

        if (currentSlotIndex >= slots.size() || bestMatch.size() == slots.size())
            return;

        List<ItemStack> validStacks = validAndAvailableContent.get(slots.get(currentSlotIndex));

        for (ItemStack requiredStack : validStacks) {

            HashMap<Integer, ItemStack> found = this.invCheckAndFind(playerInvCache, requiredStack, false);

            if (!found.isEmpty()) {
                usedPlayerSlots.put(slots.get(currentSlotIndex), found);
                if (usedPlayerSlots.size() > bestMatch.size())
                    bestMatch.putAll(usedPlayerSlots);

                this.check(slots, currentSlotIndex + 1, validAndAvailableContent, usedPlayerSlots, bestMatch, playerInvCache);
            }
        }
        this.check(slots, currentSlotIndex + 1, validAndAvailableContent, usedPlayerSlots, bestMatch, playerInvCache);

    }

    private HashMap<Integer, ItemStack> invCheckAndFind(NonNullList<ItemStack> playerInvCache, ItemStack requiredStack, boolean checkComponents) {

        HashMap<Integer, ItemStack> usedPlayerSlots = new HashMap<>();
        int requiredAmount = requiredStack.getCount();

        ItemStack firstFound = ItemStack.EMPTY;

        for (int playerSlot = 0; playerSlot < playerInvCache.size(); playerSlot++) {

            if (requiredAmount <= 0)
                break;

            ItemStack playerStack = playerInvCache.get(playerSlot);
            ItemStack foundStack = playerStack.copy();

            if (!(checkComponents ? ItemStack.isSameItemSameComponents(playerStack, requiredStack) : ItemStack.isSameItem(playerStack, requiredStack)))
                continue;

            if (firstFound.isEmpty())
                firstFound = foundStack.copy();

            if (!ItemStack.isSameItemSameComponents(foundStack, firstFound))
                continue;

            int prevReq = requiredAmount;
            requiredAmount -= Math.min(requiredAmount, playerStack.getCount());

            playerStack.setCount(playerStack.getCount() - (prevReq - requiredAmount));
            if (playerStack.getCount() <= 0)
                playerInvCache.set(playerSlot, ItemStack.EMPTY);

            foundStack.setCount(prevReq - requiredAmount);
            usedPlayerSlots.put(playerSlot, foundStack);

        }

        if (requiredAmount == 0)
            return usedPlayerSlots;
        else {
            this.returnToCache(usedPlayerSlots, playerInvCache);

            return new HashMap<>();
        }
    }

    private void returnToCache(HashMap<Integer, ItemStack> usedPlayerSlots, NonNullList<ItemStack> playerInvCache) {
        usedPlayerSlots.forEach((playerSlot, stack) -> {

            if (playerInvCache.get(playerSlot).isEmpty())
                playerInvCache.set(playerSlot, stack);
            else
                playerInvCache.get(playerSlot).setCount(playerInvCache.get(playerSlot).getCount() + stack.getCount());

        });
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
                if (!slotFillContext.contentDependencies.containsKey(j))
                    this.viewContainer.setItem(j + (i * this.getViewType().getSlotCount()), slotFillContext.contentBySlot(j).next());
            }

        }

        this.updateDependencies();
    }

    protected void updateDependencies() {
        for (int i = 0; i < this.currentDisplay.size(); i++) {
            IEivViewRecipe recipe = this.currentDisplay.get(i);

            SlotFillContext slotFillContext = new SlotFillContext();
            recipe.bindSlots(slotFillContext);

            for (int j = 0; j < this.getViewType().getSlotCount(); j++) {

                if (slotFillContext.contentDependencies.containsKey(j))
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

    public void updateTransferCache() {
        System.out.println("Updating cache...");
        this.transferData.clear();
        for (int i = 0; i < this.getCurrentDisplay().size(); i++) {
            this.transferData.add(this.checkMatchingContent(i));
        }

        this.viewScreen.checkGui();
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

        public void bindDepedantSlot(int slotId, Supplier<Integer> dependantIndex, SlotContent slotContent) {
            this.contents.put(slotId, slotContent);
            this.contentDependencies.put(slotId, dependantIndex);
        }

        public HashMap<Integer, Supplier<Integer>> contentDependencies() {
            return this.contentDependencies;
        }

        public void addAdditionalStackModifier(int slotId, AdditionalStackModifier tooltipProvider) {
            this.additionalTooltips.put(slotId, tooltipProvider);
        }

        protected HashMap<Integer, SlotContent> getContents() {
            return this.contents;
        }

        protected SlotContent contentBySlot(int slotId) {
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
