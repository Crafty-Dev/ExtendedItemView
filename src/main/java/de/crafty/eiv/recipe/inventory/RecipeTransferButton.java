package de.crafty.eiv.recipe.inventory;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RecipeTransferButton extends Button {

    private final RecipeTransferData transferData;

    protected RecipeTransferButton(RecipeTransferData transferData, int x, int y, int width, int height, Component component, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, component, onPress, createNarration);

        this.transferData = transferData;
    }



}
