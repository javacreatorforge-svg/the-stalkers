package com.redstonedev.thestalkers.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** A normal red subtitle near the bottom of the screen. */
@OnlyIn(Dist.CLIENT)
public class BehindYouOverlay implements IGuiOverlay {
    public static final BehindYouOverlay INSTANCE = new BehindYouOverlay();

    @Override
    public void render(ForgeGui gui, PoseStack pose, float partialTick, int width, int height) {
        if (BehindYouState.ticks <= 0) return;
        Minecraft mc = Minecraft.getInstance();
        Component text = Component.translatable("gui.the_stalkers.behind_you");
        int w = mc.font.width(text);
        int x = (width - w) / 2;
        int y = height - 60;
        // subtle dark backdrop like vanilla subtitles
        net.minecraft.client.gui.GuiComponent.fill(pose, x - 2, y - 2, x + w + 2, y + 10, 0x60000000);
        mc.font.drawShadow(pose, text, x, y, 0xFFFF3030);
    }
}
