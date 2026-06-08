package com.redstonedev.thestalkers.network;

import com.redstonedev.thestalkers.TheStalkers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TheStalkers.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(id++, LockViewPacket.class,
                LockViewPacket::encode, LockViewPacket::decode, LockViewPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, BehindYouPacket.class,
                BehindYouPacket::encode, BehindYouPacket::decode, BehindYouPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /** Freezes the player's camera and movement, facing a fixed yaw/pitch (Golden-Freddy style). */
    public static class LockViewPacket {
        public final int ticks; public final float yaw, pitch;
        public LockViewPacket(int ticks, float yaw, float pitch) { this.ticks = ticks; this.yaw = yaw; this.pitch = pitch; }
        public static void encode(LockViewPacket p, FriendlyByteBuf b) { b.writeInt(p.ticks); b.writeFloat(p.yaw); b.writeFloat(p.pitch); }
        public static LockViewPacket decode(FriendlyByteBuf b) { return new LockViewPacket(b.readInt(), b.readFloat(), b.readFloat()); }
        public static void handle(LockViewPacket p, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.redstonedev.thestalkers.client.overlay.ViewLockState.lock(p.ticks, p.yaw, p.pitch)));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Shows a red "Behind You" subtitle for a short time. */
    public static class BehindYouPacket {
        public final int ticks;
        public BehindYouPacket(int ticks) { this.ticks = ticks; }
        public static void encode(BehindYouPacket p, FriendlyByteBuf b) { b.writeInt(p.ticks); }
        public static BehindYouPacket decode(FriendlyByteBuf b) { return new BehindYouPacket(b.readInt()); }
        public static void handle(BehindYouPacket p, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.redstonedev.thestalkers.client.overlay.BehindYouState.trigger(p.ticks)));
            ctx.get().setPacketHandled(true);
        }
    }
}
