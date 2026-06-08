package com.redstonedev.thestalkers.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Base for the flat-image stalker entities (no model/geo/animation). */
public abstract class AbstractStalker extends Monster {

    public static final int MODE_STALK = 0, MODE_MAD = 1, MODE_WINDOW = 2,
            MODE_BEHIND = 3, MODE_LOCK = 4, MODE_CHASE = 5;

    protected static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(AbstractStalker.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(AbstractStalker.class, EntityDataSerializers.INT);

    protected AbstractStalker(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 1);
        this.entityData.define(DATA_MODE, MODE_STALK);
    }

    public int getVariant() { return this.entityData.get(DATA_VARIANT); }
    public void setVariant(int v) { this.entityData.set(DATA_VARIANT, v); }
    public int getMode() { return this.entityData.get(DATA_MODE); }
    public void setMode(int m) { this.entityData.set(DATA_MODE, m); }

    // They cannot be fought - they leave on their own terms.
    @Override public boolean hurt(DamageSource source, float amount) { return false; }
    @Override public boolean isInvulnerableTo(DamageSource source) { return !source.isBypassInvul(); }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override protected boolean shouldDespawnInPeaceful() { return false; }

    protected Player nearestRealPlayer(double range) {
        Player best = null;
        double bestSq = range * range;
        for (int i = 0; i < this.level.players().size(); i++) {
            Player p = this.level.players().get(i);
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
            double d = p.distanceToSqr(this);
            if (d < bestSq) { bestSq = d; best = p; }
        }
        return best;
    }

    /** True if the player's crosshair is roughly on this entity (and can see it). */
    protected boolean isPlayerStaringAt(Player p) {
        return isLookedAt(p, true);
    }

    /** Look check. When requireLineOfSight is false this works through glass/windows. */
    protected boolean isLookedAt(Player p, boolean requireLineOfSight) {
        double dx = this.getX() - p.getX();
        double dy = this.getEyeY() - p.getEyeY();
        double dz = this.getZ() - p.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001D) return false;
        dx /= len; dy /= len; dz /= len;
        Vec3 look = p.getViewVector(1.0F);
        double dot = look.x * dx + look.y * dy + look.z * dz;
        if (dot <= 0.93D) return false;
        return !requireLineOfSight || p.hasLineOfSight(this);
    }

    protected void faceTowards(Player p) {
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        this.setYBodyRot(yaw); this.setYHeadRot(yaw); this.setYRot(yaw);
    }

    protected void applyDarkness(Player p, int seconds) {
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, seconds * 20, 0, false, false));
    }

    protected void killPlayer(Player p) {
        p.hurt(DamageSource.mobAttack(this), 1000.0F);
    }

    /** Compute the yaw/pitch a player would need to look straight at this entity. */
    protected float lookYawFor(Player p) {
        double dx = this.getX() - p.getX();
        double dz = this.getZ() - p.getZ();
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
    }
    protected float lookPitchFor(Player p) {
        double dx = this.getX() - p.getX();
        double dy = (this.getEyeY()) - p.getEyeY();
        double dz = this.getZ() - p.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        return (float) (-Math.toDegrees(Math.atan2(dy, horiz)));
    }

    protected boolean isServerPlayer(Player p) { return p instanceof ServerPlayer; }
}
