package de.crafty.eiv.common.mixin.client.gui.screens;

import de.crafty.eiv.common.CommonEIV;
import de.crafty.eiv.common.CommonEIVClient;
import de.crafty.eiv.common.embeddings.ChatEmbedding;
import de.crafty.eiv.common.embeddings.util.EmbeddingComponentContents;
import de.crafty.eiv.common.mixin.client.gui.components.IChatComponentAccessor;
import de.crafty.eiv.common.overlay.OverlayManager;
import de.crafty.eiv.common.recipe.ClientRecipeManager;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler implements Renderable {


    @Shadow(remap = false)
    protected Font font;

    @Shadow(remap = false)
    @Final
    protected Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At("RETURN"), remap = false)
    private void renderRecipeProgress(GuiGraphicsExtractor guiGraphicsExtractor, int i, int j, float f, CallbackInfo ci) {
        String statusMsg = ClientRecipeManager.INSTANCE.status().get();

        if (!ClientRecipeManager.INSTANCE.status().isIdle())
            guiGraphicsExtractor.text(this.font, statusMsg, guiGraphicsExtractor.guiWidth() - this.font.width(statusMsg) - 2, 2, -1);
    }


    @Redirect(method = "getTooltipFromItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"), remap = false)
    private static List<Component> appendRecipeTag(ItemStack stack, Item.TooltipContext list, @Nullable Player player, TooltipFlag flag) {
        List<Component> tooltip = new java.util.ArrayList<>(stack.getTooltipLines(list, player, flag));
        CompoundTag tagTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tagTag.contains(CommonEIV.MODID + "_recipeTag")) {
            tooltip.add(
                    Component.translatable("view.eiv.tags").append(": ").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("#" + tagTag.getStringOr(CommonEIV.MODID + "_recipeTag", "Error")).withStyle(ChatFormatting.GRAY))

            );
        }

        if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof RecipeViewScreen viewScreen) {
            if (viewScreen.getHoveredSlot() != null && viewScreen.getHoveredSlot().hasItem())
                viewScreen.getMenu().getAdditionalStackModifier(viewScreen.getHoveredSlot().getContainerSlot()).addTooltip(stack, tooltip);
        }

        //TODO make more performance
        tooltip.addLast(Component.literal(CommonEIVClient.resolver().getModNameForItem(stack.getItem())).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));

        return tooltip;
    }


    @Inject(method = "extractRenderState", at = @At("TAIL"), remap = false)
    private void addEmbeddings(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!ChatEmbedding.queue().isEmpty()) {
            ChatEmbedding embedding = ChatEmbedding.poll();
            if (embedding == null)
                return;

            for (int i = 0; i < embedding.getOccupiedLines(); i++) {
                this.minecraft.gui.getChat().addPlayerMessage(MutableComponent.create(EmbeddingComponentContents.createUnbound()), null, null);
            }

            ChatEmbedding.cache(embedding);
            embedding.bindMsg(((IChatComponentAccessor) this.minecraft.gui.getChat()).getAllMessages().getFirst());
        }
    }


    @Inject(method = "extractBackground", at = @At("HEAD"), remap = false)
    private void injectOverlayBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {

        if((Object) this instanceof AbstractContainerScreen<?>)
            OverlayManager.INSTANCE.renderAllBackground(graphics, mouseX, mouseY, partialTicks);
    }

}
