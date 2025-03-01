package de.crafty.eiv.extra;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.MapCodec;
import de.crafty.eiv.ExtendedItemView;
import de.crafty.eiv.ExtendedItemViewClient;
import de.crafty.eiv.recipe.item.FluidItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class FluidItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {

    private final FluidItemModel model;

    private FluidItemSpecialRenderer(FluidItemModel model) {
        this.model = model;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay, boolean bl) {

        if (!(stack.getItem() instanceof FluidItem fluidItem))
            return;

        FluidStack fluidStack = FluidStack.fromItemStack(stack);
        Fluid fluid = fluidStack.getFluid();

        float renderHeight = Math.max(Math.min((float) fluidStack.getAmount() / (float) FluidStack.AMOUNT_FULL, 1.0F), 0.1F);



        TextureAtlasSprite sprite1 = FluidRenderHandlerRegistryImpl.INSTANCE.get(fluid).getFluidSprites(null, null, fluid.defaultFluidState())[0];
        int color = fluid == Fluids.WATER ? Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS).value().getWaterColor() : -1;
        Color unmodified = new Color(color);
        color = new Color(unmodified.getRed(), unmodified.getGreen(), unmodified.getBlue(), 255).getRGB();

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(sprite1.atlasLocation()).apply(sprite1.contents().name());



        float spriteWidthFloat = (sprite.getU1() - sprite.getU0());
        float spriteHeightFloat = (sprite.getV1() - sprite.getV0());

        spriteHeightFloat *= renderHeight;

        poseStack.pushPose();
        poseStack.scale(1.0F, 1.0F, 1.0F);
        VertexConsumer vertexConsumer = sprite.wrap(ItemRenderer.getFoilBuffer(multiBufferSource, RenderType.entityTranslucent(sprite.atlasLocation()), itemDisplayContext == ItemDisplayContext.GUI, bl));
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, 0, 0, 0).setUv(sprite.getU0(), sprite.getV0()).setOverlay(overlay).setLight(light).setColor(color).setNormal(0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix4f, 0, renderHeight, 0).setUv(sprite.getU0(), sprite.getV0() + spriteHeightFloat).setOverlay(overlay).setLight(light).setColor(color).setNormal(0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix4f, 1.0F, renderHeight, 0).setUv(sprite.getU0() + spriteWidthFloat, sprite.getV0() + spriteHeightFloat).setOverlay(overlay).setLight(light).setColor(color).setNormal(0.0F, 0.0F, 1.0F);
        vertexConsumer.addVertex(matrix4f, 1.0F, 0, 0).setUv(sprite.getU0() + spriteWidthFloat, sprite.getV0()).setOverlay(overlay).setLight(light).setColor(color).setNormal(0.0F, 0.0F, 1.0F);
        poseStack.popPose();

    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack itemStack) {
        return itemStack;
    }

    public static void renderGuiSprite(PoseStack poseStack, VertexConsumer vertexConsumer, TextureAtlasSprite sprite, int light, int overlay) {


        poseStack.pushPose();


        poseStack.popPose();
    }

    public static void renderTexturedPlane(PoseStack.Pose pose, VertexConsumer consumer, TextureAtlasSprite texture, Direction facing, float x, float y, float z, float width, float height, float u, float v, float texWidth, float texHeight, int color, int light) {

        float u0 = u * texture.contents().width() / 16.0F;
        float v0 = v * texture.contents().height() / 16.0F;

        float u1 = u0 + texWidth * texture.contents().width() / 16.0F;
        float v1 = v0 + texHeight * texture.contents().height() / 16.0F;

        Vec3i normal = facing.getUnitVec3i();
        float xNormal = normal.getX();
        float yNormal = normal.getY();
        float zNormal = normal.getZ();

        if (facing == Direction.UP) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(texture.getU(u0), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y, z + height).setColor(color).setUv(texture.getU(u0), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y, z + height).setColor(color).setUv(texture.getU(u1), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y, z).setColor(color).setUv(texture.getU(u1), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
        }

        if (facing == Direction.DOWN) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(texture.getU(u0), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y, z).setColor(color).setUv(texture.getU(u1), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y, z + height).setColor(color).setUv(texture.getU(u1), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y, z + height).setColor(color).setUv(texture.getU(u0), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
        }


        if (facing == Direction.NORTH) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(texture.getU(u1), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y + height, z).setColor(color).setUv(texture.getU(u1), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y + height, z).setColor(color).setUv(texture.getU(u0), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y, z).setColor(color).setUv(texture.getU(u0), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
        }

        if (facing == Direction.SOUTH) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(texture.getU(u1), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y, z).setColor(color).setUv(texture.getU(u0), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x + width, y + height, z).setColor(color).setUv(texture.getU(u0), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y + height, z).setColor(color).setUv(texture.getU(u1), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);

        }

        if (facing == Direction.WEST) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(texture.getU(u1), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y, z + width).setColor(color).setUv(texture.getU(u0), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y + height, z + width).setColor(color).setUv(texture.getU(u0), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y + height, z).setColor(color).setUv(texture.getU(u1), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
        }

        if (facing == Direction.EAST) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(texture.getU(u1), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y + height, z).setColor(color).setUv(texture.getU(u1), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y + height, z + width).setColor(color).setUv(texture.getU(u0), texture.getV(v0)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
            consumer.addVertex(pose, x, y, z + width).setColor(color).setUv(texture.getU(u0), texture.getV(v1)).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(xNormal, yNormal, zNormal);
        }

    }


    @Environment(EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @NotNull SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new FluidItemSpecialRenderer(new FluidItemModel(entityModelSet.bakeLayer(ExtendedItemViewClient.FLUID_ITEM_MODEL_LAYER)));
        }

        @Override
        public @NotNull MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
