package com.redstonedev.thestalkers.client.renderer;

import com.redstonedev.thestalkers.TheStalkers;
import com.redstonedev.thestalkers.entity.GoatStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoatStalkerRenderer extends BillboardStalkerRenderer<GoatStalkerEntity> {
    public GoatStalkerRenderer(EntityRendererProvider.Context ctx) { super(ctx, 2.44F); }
    @Override
    public ResourceLocation getTextureLocation(GoatStalkerEntity e) {
        int v = e.getVariant();
        String name = (v == 5) ? "goatjumpscare" : ("goat" + Math.max(1, Math.min(4, v)));
        return new ResourceLocation(TheStalkers.MODID, "textures/entity/" + name + ".png");
    }
}
