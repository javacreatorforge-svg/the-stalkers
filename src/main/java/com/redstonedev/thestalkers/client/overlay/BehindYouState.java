package com.redstonedev.thestalkers.client.overlay;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BehindYouState {
    private BehindYouState() {}
    public static volatile int ticks = 0;
    public static void trigger(int t) { ticks = t; }
    public static void clientTick() { if (ticks > 0) ticks--; }
}
