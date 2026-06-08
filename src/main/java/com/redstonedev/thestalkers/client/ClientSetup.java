package com.redstonedev.thestalkers.client;

import com.redstonedev.thestalkers.TheStalkers;
import com.redstonedev.thestalkers.client.overlay.BehindYouOverlay;
import com.redstonedev.thestalkers.client.overlay.BehindYouState;
import com.redstonedev.thestalkers.client.overlay.ViewLockState;
import com.redstonedev.thestalkers.client.renderer.GoatStalkerRenderer;
import com.redstonedev.thestalkers.client.renderer.StalkerRenderer;
import com.redstonedev.thestalkers.init.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.STALKER.get(), StalkerRenderer::new);
            EntityRenderers.register(ModEntities.GOAT_STALKER.get(), GoatStalkerRenderer::new);
        });
    }

    @Mod.EventBusSubscriber(modid = TheStalkers.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.SUBTITLES.id(), "behind_you", BehindYouOverlay.INSTANCE);
        }
    }

    @Mod.EventBusSubscriber(modid = TheStalkers.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                ViewLockState.clientTick();
                BehindYouState.clientTick();
            }
        }
        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            if (ViewLockState.isLocked()) {
                event.getInput().leftImpulse = 0.0F;
                event.getInput().forwardImpulse = 0.0F;
                event.getInput().up = event.getInput().down = false;
                event.getInput().left = event.getInput().right = false;
                event.getInput().jumping = false;
                event.getInput().shiftKeyDown = false;
            }
        }
    }
}
