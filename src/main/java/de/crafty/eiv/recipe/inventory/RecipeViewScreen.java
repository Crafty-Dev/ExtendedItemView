package de.crafty.eiv.recipe.inventory;

import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.api.recipe.IEivViewRecipe;
import de.crafty.eiv.network.payload.transfer.ServerboundTransferPayload;
import de.crafty.eiv.overlay.ItemViewOverlay;
import de.crafty.eiv.recipe.rendering.AnimationTicker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RecipeViewScreen extends AbstractContainerScreen<RecipeViewMenu> {

    private static final ResourceLocation VIEW_LOCATION = ResourceLocation.fromNamespaceAndPath(ExtendedItemView.MODID, "textures/gui/recipe_view.png");

    //Timestamp when opening the view
    private final long timestamp;

    private Button prevRecipe, nextRecipe;
    private Component guiTitle, page;

    private final List<AnimationTicker> animationTickers;
    private final HashMap<ResourceLocation, Integer> animationTickCache;

    private final List<Button> transferButtons;

    //View Type
    private final List<ViewTypeButton> viewTypeButtons;
    private int viewTypePage;
    private Button prevTypePage, nextTypePage;

    public RecipeViewScreen(RecipeViewMenu recipeViewMenu, Inventory inventory, Component component) {
        super(recipeViewMenu, inventory, component);

        this.transferButtons = new ArrayList<>();
        this.viewTypeButtons = new ArrayList<>();
        this.viewTypePage = 0;

        this.animationTickers = new ArrayList<>();
        this.animationTickCache = new HashMap<>();

        this.imageHeight = this.getMenu().getHeight();
        this.imageWidth = this.getMenu().getWidth();

        this.guiTitle = component;
        this.page = this.createPageComponent();


        this.timestamp = inventory.player.level().getGameTime();
        recipeViewMenu.setViewScreen(this);
    }


    private Component createPageComponent() {
        return Component.literal((this.getMenu().getCurrentPage() + 1) + "/" + (this.getMenu().getMaxPageIndex() + 1));
    }

    @Override
    protected void init() {
        super.init();

        this.prevRecipe = Button.builder(Component.literal("<"), button -> {
                    this.getMenu().prevPage();
                })
                .size(12, 12)
                .build();

        this.nextRecipe = Button.builder(Component.literal(">"), button -> {
                    this.getMenu().nextRecipe();
                })
                .size(12, 12)
                .build();

        this.prevTypePage = Button.builder(Component.literal("<"), button -> {
                    this.viewTypePage = Math.max(this.viewTypePage - 1, 0);
                    this.checkGui();
                })
                .size(12, 12)
                .build();

        this.nextTypePage = Button.builder(Component.literal(">"), button -> {
                    this.viewTypePage = Math.min(this.viewTypePage + 1, this.getMenu().getViewTypeOrder().size() / 5);
                    this.checkGui();
                })
                .size(12, 12)
                .build();

        this.checkGui();

        this.addRenderableWidget(this.prevRecipe);
        this.addRenderableWidget(this.nextRecipe);
        this.addRenderableWidget(this.prevTypePage);
        this.addRenderableWidget(this.nextTypePage);

        int width = 24;
        int height = 24;

        this.viewTypeButtons.clear();
        for (int i = 0; i < this.getMenu().getViewTypeOrder().size(); i++) {
            int tempId = i % 5;

            int xPos = this.width / 2 - (5 * width / 2 + 4 * 2 / 2) + tempId * width + tempId * 2;
            int yPos = this.topPos - height - 1;

            this.viewTypeButtons.add(new ViewTypeButton(this, xPos, yPos, width, height, this.getMenu().getViewTypeOrder().get(i), i));
        }
    }

    protected void checkGui() {

        this.prevRecipe.active = this.getMenu().hasPrevRecipe();
        this.nextRecipe.active = this.getMenu().hasNextRecipe();

        this.prevTypePage.visible = this.viewTypePage > 0;
        this.nextTypePage.visible = this.viewTypePage < (this.getMenu().getViewTypeOrder().size() - 1) / 5;

        this.imageHeight = this.getMenu().getHeight();
        this.imageWidth = this.getMenu().getWidth();

        this.topPos = 32;

        this.prevRecipe.setPosition(this.leftPos + 8, this.topPos + 4);
        this.nextRecipe.setPosition(this.leftPos + this.imageWidth - 8 - 12, this.topPos + 4);

        this.prevTypePage.setPosition(this.width / 2 - (5 * 24 + 4 * 2) / 2 - 2 - 12, this.topPos - 1 - 12 - 6);
        this.nextTypePage.setPosition(this.width / 2 + (5 * 24 + 4 * 2) / 2 + 2, this.topPos - 1 - 12 - 6);

        this.guiTitle = this.getMenu().getViewType().getDisplayName();
        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.guiTitle) / 2;

        this.page = this.createPageComponent();

        this.animationTickCache.clear();
        this.checkTickers();


        //Transfer Button Logic
        this.transferButtons.forEach(this::removeWidget);
        this.transferButtons.clear();

        int guiLeft = this.leftPos + this.getMenu().guiOffsetLeft();

        for (int i = 0; i < this.getMenu().getCurrentDisplay().size(); i++) {
            final IEivViewRecipe currentView = this.getMenu().getCurrentDisplay().get(i);

            int guiTop = this.topPos + this.getMenu().guiOffsetTop(i);

            int finalI = i;
            Button button = Button.builder(Component.literal("+"), button1 -> {
                        if (!currentView.supportsItemTransfer())
                            return;

                        Minecraft.getInstance().setScreen(this.getMenu().getParentScreen());
                        LocalPlayer player = Minecraft.getInstance().player;

                        if (Minecraft.getInstance().screen != null && player != null) {
                            IEivViewRecipe.RecipeTransferMap map = new IEivViewRecipe.RecipeTransferMap();
                            currentView.mapRecipeItems(map);


                            if (currentView.getTransferClass() != null && currentView.getTransferClass().isInstance(Minecraft.getInstance().screen)) {

                                RecipeTransferData transferData = this.getMenu().getTransferData().get(finalI);

                                HashMap<Integer, HashMap<Integer, ItemStack>> usedPlayerSlots = RecipeViewScreen.hasShiftDown() ? transferData.getStackedData().getUsedPlayerSlots() : transferData.getUsedPlayerSlots();
                                //TODO make component required in recipes
                                ClientPlayNetworking.send(new ServerboundTransferPayload(map.getTransferMap(), usedPlayerSlots));

                            }
                        }

                    })
                    .size(12, 12)
                    .pos(guiLeft + currentView.getViewType().getDisplayWidth() + 4, guiTop + currentView.getViewType().getDisplayHeight() / 2 - 6)
                    .build();

            RecipeTransferData data = this.getMenu().getTransferData().get(i);
            button.active = data.isSuccess() && currentView.supportsItemTransfer() && currentView.getTransferClass().isInstance(this.getMenu().getParentScreen());
            button.visible = currentView.supportsItemTransfer();

            this.addRenderableWidget(button);
            this.transferButtons.add(button);

        }

    }

    private void checkTickers() {
        this.animationTickers.forEach(animationTicker -> {
            this.animationTickCache.put(animationTicker.id(), animationTicker.getTick());
        });

        this.animationTickers.clear();

        this.getMenu().getCurrentDisplay().forEach(recipe -> {
            recipe.getAnimationTickers().forEach(animationTicker -> {
                this.animationTickers.add(animationTicker);

                if (this.animationTickCache.containsKey(animationTicker.id()))
                    animationTicker.setTick(this.animationTickCache.get(animationTicker.id()));
                else
                    animationTicker.resetTick();
            });
        });
    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
        guiGraphics.drawString(this.font, this.guiTitle, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.page, (this.imageWidth - font.width(this.page)) / 2, this.imageHeight - 12, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }


    @Override
    protected @NotNull List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
        List<Component> tooltip = super.getTooltipFromContainerItem(itemStack);

        CompoundTag tagTag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tagTag.contains(ExtendedItemView.MODID + "_recipeTag")) {
            tooltip.add(
                    Component.translatable("view.eiv.tags").append(": ").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("#" + tagTag.getString(ExtendedItemView.MODID + "_recipeTag")).withStyle(ChatFormatting.GRAY))

            );
        }

        if (this.hoveredSlot != null && this.hoveredSlot.hasItem())
            this.getMenu().getAdditionalStackModifier(this.hoveredSlot.getContainerSlot()).addTooltip(itemStack, tooltip);

        //TODO make more performance
        String modId = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace();

        FabricLoader.getInstance().getModContainer(modId).ifPresent(container -> {
            tooltip.addLast(Component.literal(container.getMetadata().getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        });

        return tooltip;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        if (!(mouseX >= this.leftPos && mouseX <= this.leftPos + this.imageWidth && mouseY >= this.topPos && mouseY <= this.topPos + this.imageHeight))
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        if (scrollY < 0) {
            this.getMenu().nextPage();
            this.checkTickers();
        }
        if (scrollY > 0) {
            this.getMenu().prevPage();
            this.checkTickers();
        }

        if (scrollY != 0)
            this.page = this.createPageComponent();

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (mouseButton == 1 && this.hoveredSlot != null) {
            ItemViewOverlay.INSTANCE.openRecipeView(this.hoveredSlot.getItem(), ItemViewOverlay.ItemViewOpenType.INPUT);
            return true;
        }

        if (mouseButton == 0 && this.hoveredSlot != null) {
            ItemViewOverlay.INSTANCE.openRecipeView(this.hoveredSlot.getItem(), ItemViewOverlay.ItemViewOpenType.RESULT);
            return true;
        }

        if (mouseButton == 0) {

            for (int i = this.viewTypePage * 5; i < this.viewTypePage * 5 + 5 && this.viewTypeButtons.size() > i; i++) {
                if (this.viewTypeButtons.get(i).onClick(mouseButton, (int) mouseX, (int) mouseY))
                    return true;
            }

        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean isPrevTypeHovered(double mouseX, double mouseY) {
        return mouseX >= this.leftPos - 14 - 2 && mouseX <= this.leftPos - 2 && mouseY >= this.topPos + 2 && mouseY <= this.topPos + 2 + 14;
    }

    private boolean isNextTypeHovered(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + this.imageWidth + 2 && mouseX <= this.leftPos + this.imageWidth + 2 + 14 && mouseY >= this.topPos + 2 && mouseY <= this.topPos + 2 + 14;
    }

    @Override
    protected void containerTick() {
        this.animationTickers.forEach(AnimationTicker::tick);

        if (this.minecraft == null || this.minecraft.player == null)
            return;

        long timeOpen = (this.minecraft.player.clientLevel.getGameTime() - this.timestamp);

        if (timeOpen % 25 == 0 && timeOpen >= 25)
            this.getMenu().tickContents();
    }

    public int getLeftPos() {
        return this.leftPos;
    }

    public int getTopPos() {
        return this.topPos;
    }

    public int getGuiWidth() {
        return this.imageWidth;
    }

    public int getGuiHeight() {
        return this.imageHeight;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {


        guiGraphics.blit(RenderType::guiTextured, VIEW_LOCATION, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight - 3, 256, 256);
        guiGraphics.blit(RenderType::guiTextured, VIEW_LOCATION, this.leftPos, this.topPos + (this.imageHeight - 3), 0, 256 - 3, this.imageWidth, 3, 256, 256);


        IEivRecipeViewType viewType = this.getMenu().getViewType();

        //Render icons

        int current = this.getMenu().getCurrentTypeIndex();

        for (int i = 0; i < 5; i++) {

            guiGraphics.blit(RenderType::guiTextured, VIEW_LOCATION, this.width / 2 - (5 * 24 + 4 * 2) / 2 + i * 24 + i * 2, this.topPos - 24 - 1, 208, 0, 24, 24, 256, 256);
        }

        for (int i = this.viewTypePage * 5; i < this.viewTypePage * 5 + 5 && this.viewTypeButtons.size() > i; i++) {
            this.viewTypeButtons.get(i).render(guiGraphics, mouseX, mouseY, partialTicks);
        }


        //Render craft references

        List<ItemStack> craftReferences = this.getMenu().getViewType().getCraftReferences();

        for (int i = 0; i < craftReferences.size(); i++) {

            guiGraphics.blit(RenderType::guiTextured, VIEW_LOCATION, this.leftPos - 25, this.topPos + 4 + i * 24 + i, 231, 48, 25, 24, 256, 256);

        }


        int guiLeft = this.leftPos + this.getMenu().guiOffsetLeft();

        for (int i = 0; i < this.getMenu().getCurrentDisplay().size(); i++) {

            int guiTop = this.topPos + this.getMenu().guiOffsetTop(i);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(guiLeft, guiTop, 0);

            guiGraphics.blit(RenderType::guiTextured, viewType.getGuiTexture(), 0, 0, 0, 0, viewType.getDisplayWidth(), viewType.getDisplayHeight(), viewType.getDisplayWidth(), viewType.getDisplayHeight());
            this.renderInvalidSlots(guiGraphics, i);
            this.getMenu().getCurrentDisplay().get(i).renderRecipe(this, guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.pose().popPose();
        }

    }

    private void renderInvalidSlots(GuiGraphics guiGraphics, int displayId) {
        Button button = this.transferButtons.get(displayId);
        if (!button.isHovered())
            return;

        IEivViewRecipe current = this.getMenu().getCurrentDisplay().get(displayId);

        RecipeTransferData data = this.getMenu().getTransferData().get(displayId);
        if (data.isSuccess())
            return;

        for (int slotId : data.getSlotResults().keySet()) {

            if (data.getSlotResults().get(slotId))
                continue;

            int actualSlotId = slotId + (displayId * current.getViewType().getSlotCount());
            Slot invSlot = this.getMenu().getSlot(actualSlotId);

            int x = invSlot.x;
            int y = invSlot.y;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(-this.getMenu().guiOffsetLeft(), -this.getMenu().guiOffsetTop(displayId), 0);
            guiGraphics.fill(x, y, x + 16, y + 16, new Color(255, 0, 0, 64).getRGB());
            guiGraphics.pose().popPose();

        }
    }

    @Override
    public void onClose() {
        super.onClose();

        Minecraft.getInstance().setScreen(this.getMenu().getParentScreen());
    }


    record ViewTypeButton(RecipeViewScreen viewScreen, int x, int y, int width, int height, IEivRecipeViewType viewType,
                          int viewTypeId) {


        private boolean onClick(int mouseButton, int mouseX, int mouseY) {
            if (!(mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height))
                return false;

            this.viewScreen.getMenu().setViewType(this.viewTypeId);
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        private void onHover(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (!(mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height))
                return;

            guiGraphics.renderTooltip(Minecraft.getInstance().font, this.viewType.getDisplayName(), mouseX, mouseY);
        }

        private void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            guiGraphics.blit(RenderType::guiTextured, VIEW_LOCATION, this.x(), this.y(), 232, this.viewType() == this.viewScreen.getMenu().getViewType() ? 24 : 0, 24, 24, 256, 256);
            guiGraphics.renderFakeItem(this.viewType().getIcon(), this.x() + 4, this.y() + 4);

            this.onHover(guiGraphics, mouseX, mouseY);
        }

    }
}

