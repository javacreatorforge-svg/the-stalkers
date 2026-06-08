package com.redstonedev.thestalkers.client.renderer;

import com.redstonedev.thestalkers.TheStalkers;
import com.redstonedev.thestalkers.entity.StalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StalkerRenderer extends BillboardStalkerRenderer<StalkerEntity> {
    public StalkerRenderer(EntityRendererProvider.Context ctx) { super(ctx, 3.35F); }
    @Override
    public ResourceLocation getTextureLocation(StalkerEntity e) {
        int v = Math.max(1, Math.min(8, e.getVariant()));
        return new ResourceLocation(TheStalkers.MODID, "textures/entity/stalker" + v + ".png");
    }
}
