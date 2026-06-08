package com.redstonedev.thestalkers.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.redstonedev.thestalkers.entity.AbstractStalker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Renders a stalker as a flat camera-facing image (no model). */
@OnlyIn(Dist.CLIENT)
public abstract class BillboardStalkerRenderer<T extends AbstractStalker> extends EntityRenderer<T> {

    private final float height;

    protected BillboardStalkerRenderer(EntityRendererProvider.Context ctx, float height) {
        super(ctx);
        this.shadowRadius = 0.0F;
        this.height = height;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        ResourceLocation tex = getTextureLocation(entity);
        pose.pushPose();
        pose.translate(0.0D, height / 2.0D, 0.0D);
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        Matrix4f mat = pose.last().pose();
        Matrix3f norm = pose.last().normal();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
        int light = 0x00F000F0; // full bright
        float h = height / 2.0F, w = height / 2.0F; // square images
        vertex(vc, mat, norm, -w, -h, 0.0F, 1.0F, light);
        vertex(vc, mat, norm,  w, -h, 1.0F, 1.0F, light);
        vertex(vc, mat, norm,  w,  h, 1.0F, 0.0F, light);
        vertex(vc, mat, norm, -w,  h, 0.0F, 0.0F, light);

        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }

    private void vertex(VertexConsumer vc, Matrix4f mat, Matrix3f norm,
                        float x, float y, float u, float v, int light) {
        vc.vertex(mat, x, y, 0.0F).color(255, 255, 255, 255).uv(u, v)
          .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
