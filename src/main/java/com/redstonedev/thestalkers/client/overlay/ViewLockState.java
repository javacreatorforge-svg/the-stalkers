package com.redstonedev.thestalkers.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Freezes the player's camera + movement (Golden-Freddy style) for a number of ticks. */
@OnlyIn(Dist.CLIENT)
public final class ViewLockState {
    private ViewLockState() {}
    public static volatile int ticks = 0;
    public static float yaw, pitch;

    public static void lock(int t, float y, float p) { ticks = t; yaw = y; pitch = p; }
    public static boolean isLocked() { return ticks > 0; }

    public static void clientTick() {
        if (ticks <= 0) return;
        ticks--;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.setYRot(yaw); mc.player.setXRot(pitch);
            mc.player.yRotO = yaw; mc.player.xRotO = pitch;
            mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
        }
    }
}
