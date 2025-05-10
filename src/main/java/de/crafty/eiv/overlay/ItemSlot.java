package de.crafty.eiv.overlay;

import de.crafty.eiv.ExtendedItemViewClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ItemSlot {

    private final ItemStack stack;
    private final int x, y;

    private boolean hovered;

    public ItemSlot(ItemStack stack, int x, int y) {
        this.stack = stack;

        this.x = x;
        this.y = y;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.hovered = this.isMouseOver(mouseX, mouseY);

        Minecraft mc = Minecraft.getInstance();
        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(mc, this.stack));

        String modId = BuiltInRegistries.ITEM.getKey(this.stack.getItem()).getNamespace();
        FabricLoader.getInstance().getModContainer(modId).ifPresent(container -> {
            tooltip.addLast(Component.literal(container.getMetadata().getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
        });

        if (this.isHovered())
            guiGraphics.fill(this.x, this.y, this.x + 20, this.y + 20, new Color(255, 255, 255, 128).getRGB());

        guiGraphics.renderItem(this.stack, this.x + 2, this.y + 2);

        if (this.isHovered()) {
            guiGraphics.renderTooltip(mc.font, tooltip, this.stack.getTooltipImage(), mouseX, mouseY);

        }
    }

    protected void onClicked(int mouseX, int mouseY, int mouseButton) {


        LocalPlayer clientPlayer = Minecraft.getInstance().player;

        if(clientPlayer == null)
            return;



        if(mouseButton == 0)
            ItemViewOverlay.INSTANCE.openRecipeView(this.stack, ItemViewOverlay.ItemViewOpenType.RESULT);

        if(mouseButton == 1)
            ItemViewOverlay.INSTANCE.openRecipeView(this.stack, ItemViewOverlay.ItemViewOpenType.INPUT);
    }

    boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX < this.x + 20 && mouseY >= this.y && mouseY < this.y + 20;
    }

    public boolean isHovered(){
        return this.hovered;
    }
}
