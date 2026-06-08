package com.redstonedev.thestalkers.event;

import com.redstonedev.thestalkers.entity.AbstractStalker;
import com.redstonedev.thestalkers.entity.GoatStalkerEntity;
import com.redstonedev.thestalkers.entity.StalkerEntity;
import com.redstonedev.thestalkers.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class StalkerSpawns {
    private StalkerSpawns() {}
    private static final Random RNG = new Random();

    // ---------- The Stalker ----------
    public static void stalkerStalk(ServerLevel level, Player player) {
        BlockPos pos = farFrom(level, player);
        if (pos == null) return;
        StalkerEntity e = ModEntities.STALKER.get().create(level);
        if (e == null) return;
        int v = weightedStalkerVariant();
        e.setVariant(v);
        e.setMode(v == 5 ? AbstractStalker.MODE_MAD : AbstractStalker.MODE_STALK);
        place(level, e, pos, player);
        e.onSpawnedWarn();
    }

    public static void stalkerMad(ServerLevel level, Player player) {
        BlockPos pos = farFrom(level, player);
        if (pos == null) return;
        StalkerEntity e = ModEntities.STALKER.get().create(level);
        if (e == null) return;
        e.setVariant(5);
        e.setMode(AbstractStalker.MODE_MAD);
        place(level, e, pos, player);
        e.onSpawnedWarn();
    }

    public static boolean stalkerWindow(ServerLevel level, Player player) {
        BlockPos pos = inFrontOfWindow(level, player);
        if (pos == null) return false;
        StalkerEntity e = ModEntities.STALKER.get().create(level);
        if (e == null) return false;
        e.setVariant(8);
        e.setMode(AbstractStalker.MODE_WINDOW);
        place(level, e, pos, player);
        return true;
    }

    public static void stalkerBehind(ServerLevel level, Player player) {
        BlockPos pos = behind(level, player);
        if (pos == null) return;
        StalkerEntity e = ModEntities.STALKER.get().create(level);
        if (e == null) return;
        e.setVariant(5); // behind always uses the stalker5 image
        e.setMode(AbstractStalker.MODE_BEHIND);
        place(level, e, pos, player);
    }

    // ---------- The Goat Stalker ----------
    public static void goatStalk(ServerLevel level, Player player) {
        BlockPos pos = farFrom(level, player);
        if (pos == null) return;
        GoatStalkerEntity e = ModEntities.GOAT_STALKER.get().create(level);
        if (e == null) return;
        e.setVariant(weightedGoatVariant());
        e.setMode(AbstractStalker.MODE_STALK);
        place(level, e, pos, player);
    }

    public static void goatChase(ServerLevel level, Player player) {
        BlockPos pos = farFrom(level, player);
        if (pos == null) return;
        GoatStalkerEntity e = ModEntities.GOAT_STALKER.get().create(level);
        if (e == null) return;
        e.setVariant(4);
        e.setMode(AbstractStalker.MODE_CHASE);
        place(level, e, pos, player);
    }

    public static boolean goatWindow(ServerLevel level, Player player) {
        BlockPos pos = inFrontOfWindow(level, player);
        if (pos == null) return false;
        GoatStalkerEntity e = ModEntities.GOAT_STALKER.get().create(level);
        if (e == null) return false;
        e.setVariant(3);
        e.setMode(AbstractStalker.MODE_WINDOW);
        place(level, e, pos, player);
        return true;
    }

    public static void goatBehind(ServerLevel level, Player player) {
        BlockPos pos = behind(level, player);
        if (pos == null) return;
        GoatStalkerEntity e = ModEntities.GOAT_STALKER.get().create(level);
        if (e == null) return;
        e.setVariant(5); // goatjumpscare
        e.setMode(AbstractStalker.MODE_BEHIND);
        place(level, e, pos, player);
    }

    // ---------- helpers ----------
    private static int weightedStalkerVariant() {
        int r = RNG.nextInt(100);
        if (r < 34) return 1;       // most common
        if (r < 50) return 2;
        if (r < 64) return 3;
        if (r < 78) return 4;
        if (r < 90) return 6;
        if (r < 95) return 5;       // rare -> mad
        return 7;                   // rare -> lock
    }

    private static int weightedGoatVariant() {
        int r = RNG.nextInt(100);
        if (r < 55) return 4;       // most common
        if (r < 80) return 1;
        return 2;
    }

    private static void place(ServerLevel level, AbstractStalker e, BlockPos pos, Player player) {
        e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0F, 0F);
        e.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        double dx = player.getX() - e.getX(), dz = player.getZ() - e.getZ();
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        e.setYRot(yaw); e.setYBodyRot(yaw); e.setYHeadRot(yaw);
        level.addFreshEntity(e);
    }

    private static BlockPos farFrom(ServerLevel level, Player player) {
        BlockPos origin = player.blockPosition();
        for (int i = 0; i < 24; i++) {
            double a = RNG.nextDouble() * Math.PI * 2.0;
            double d = 18 + RNG.nextInt(16);
            int x = origin.getX() + (int) Math.round(Math.cos(a) * d);
            int z = origin.getZ() + (int) Math.round(Math.sin(a) * d);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos c = new BlockPos(x, y, z);
            if (level.getBlockState(c).isAir() && !level.getBlockState(c.below()).isAir()) return c;
        }
        return null;
    }

    private static BlockPos behind(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle();
        double horiz = Math.sqrt(look.x * look.x + look.z * look.z);
        double nx = horiz > 0.001 ? look.x / horiz : 0;
        double nz = horiz > 0.001 ? look.z / horiz : 1;
        // Right behind the player, very close, at the player's own level.
        int x = (int) Math.floor(player.getX() - nx * 1.6);
        int z = (int) Math.floor(player.getZ() - nz * 1.6);
        return new BlockPos(x, player.blockPosition().getY(), z);
    }

    private static BlockPos inFrontOfWindow(ServerLevel level, Player player) {
        BlockPos origin = player.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -12; dx <= 12; dx++)
            for (int dy = -4; dy <= 5; dy++)
                for (int dz = -12; dz <= 12; dz++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    if (!isWindow(level.getBlockState(p))) continue;
                    double wdist = p.distSqr(origin);
                    if (wdist < 4.0) continue; // window must be at least ~2 blocks away
                    // air pocket on the player's side of the glass = "in front of the window"
                    int ox = (int) Math.signum(origin.getX() - p.getX());
                    int oz = (int) Math.signum(origin.getZ() - p.getZ());
                    BlockPos front = p.offset(ox, 0, oz);
                    if (!level.getBlockState(front).isAir()) continue;
                    if (front.distSqr(origin) < 2.25) continue; // not right on top of the player
                    if (wdist < bestDist) { bestDist = wdist; best = front; }
                }
        return best; // null if no window nearby
    }

    private static boolean isWindow(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.IMPERMEABLE) // all glass blocks
                || state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock; // glass panes / bars
    }
}
