package com.redstonedev.thestalkers.event;

import com.redstonedev.thestalkers.command.StalkersCommand;
import com.redstonedev.thestalkers.init.ModEntities;
import com.redstonedev.thestalkers.init.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ForgeEvents {
    private static final Random RNG = new Random();
    private int tickCounter = 0;

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        StalkersCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) return;
        tickCounter++;
        if (tickCounter % 100 != 0) return; // ~5s
        for (ServerLevel level : event.getServer().getAllLevels()) tick(level);
    }

    private List<ServerPlayer> realPlayers(ServerLevel level) {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players())
            if (!p.isCreative() && !p.isSpectator() && p.isAlive()) out.add(p);
        return out;
    }

    private void tick(ServerLevel level) {
        List<ServerPlayer> players = realPlayers(level);
        if (players.isEmpty()) return;

        // knock plays randomly.
        if (RNG.nextInt(100) < 7) {
            ServerPlayer p = players.get(RNG.nextInt(players.size()));
            level.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.KNOCK.get(), SoundSource.HOSTILE, 0.8F, 1.0F);
        }

        boolean hasStalker = !level.getEntities(ModEntities.STALKER.get(), e -> !e.isRemoved()).isEmpty();
        boolean hasGoat = !level.getEntities(ModEntities.GOAT_STALKER.get(), e -> !e.isRemoved()).isEmpty();

        // The Stalker stalks a BUNCH (day and night). Only ever one.
        if (!hasStalker && RNG.nextInt(100) < 4) {
            ServerPlayer p = players.get(RNG.nextInt(players.size()));
            int t = RNG.nextInt(100);
            if (t < 70) StalkerSpawns.stalkerStalk(level, p);
            else if (t < 85) StalkerSpawns.stalkerWindow(level, p);
            else StalkerSpawns.stalkerBehind(level, p);
        }

        // The Goat Stalker stalks not that much. Only ever one.
        if (!hasGoat && RNG.nextInt(100) < 2) {
            ServerPlayer p = players.get(RNG.nextInt(players.size()));
            int t = RNG.nextInt(100);
            if (t < 60) StalkerSpawns.goatStalk(level, p);
            else if (t < 80) StalkerSpawns.goatWindow(level, p);
            else if (t < 95) StalkerSpawns.goatBehind(level, p);
            else StalkerSpawns.goatChase(level, p);
        }
    }
}
