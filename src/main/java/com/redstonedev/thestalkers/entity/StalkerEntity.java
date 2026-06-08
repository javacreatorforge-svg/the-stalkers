package com.redstonedev.thestalkers.entity;

import com.redstonedev.thestalkers.init.ModSounds;
import com.redstonedev.thestalkers.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class StalkerEntity extends AbstractStalker {

    private boolean staring = false;
    private int stareTimer = 0;
    private int lockTimer = 0;
    private int lifeTimer = 0;        // for MAD: 2-minute lifespan
    private int behindTimer = 0;
    private boolean behindWarned = false;
    private boolean decided = false;  // already rolled the look-reaction
    private int teleportCooldown = 20;
    private int pauseTimer = 0;
    private int ignoredTimer = 0;
    private Player lockTarget = null;

    public StalkerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.ATTACK_DAMAGE, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 80.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    /** Play the warn sound when this stalker first appears (not for the behind/window variants' silence rules). */
    public void onSpawnedWarn() {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.STALKER_WARN.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide) return;

        int mode = getMode();
        Player player = nearestRealPlayer(80.0D);

        if (mode == MODE_MAD)   { tickMad(player);   return; }
        if (mode == MODE_WINDOW){ tickWindow(player); return; }
        if (mode == MODE_BEHIND){ tickBehind(player); return; }
        if (mode == MODE_LOCK)  { tickLock(player);  return; }

        // ---- MODE_STALK (stands still and watches) ----
        if (player == null) { this.getNavigation().stop(); return; }
        this.getNavigation().stop();
        faceTowards(player);

        if (staring) { if (--stareTimer <= 0) this.discard(); return; }

        // Get close to it while it stalks -> it vanishes.
        if (this.distanceTo(player) < 3.5D) { this.discard(); return; }

        if (isPlayerStaringAt(player)) {
            ignoredTimer = 0;
            if (!decided) {
                decided = true;
                if (getVariant() == 7) {
                    lockTarget = player;
                    setMode(MODE_LOCK);
                    lockTimer = 60;
                    if (isServerPlayer(player)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                                new PacketHandler.LockViewPacket(60, lookYawFor(player), lookPitchFor(player)));
                    }
                    return;
                }
                if (this.random.nextFloat() < 0.5F) this.discard();
                else { staring = true; stareTimer = 60; } // stares back 3s then vanishes
            }
        } else {
            ignoredTimer++;
            if (ignoredTimer >= 1800) this.discard(); // ignored ~90s -> moves on
        }
    }

    // stalker5 behaviour: teleports forward through walls, random 5s pauses, kills then leaves.
    private void tickMad(Player player) {
        this.noPhysics = true;
        this.setNoGravity(true);
        lifeTimer++;
        if (player == null) { if (lifeTimer >= 2400) this.discard(); return; }
        faceTowards(player);

        if (pauseTimer > 0) { pauseTimer--; this.setDeltaMovement(Vec3.ZERO); }
        else {
            if (--teleportCooldown <= 0) {
                teleportCooldown = 8 + this.random.nextInt(6);
                Vec3 dir = player.position().subtract(this.position());
                double len = dir.length();
                if (len > 0.001D) {
                    dir = dir.scale(1.0D / len);
                    double hop = 2.0D;
                    this.setPos(this.getX() + dir.x * hop, player.getY(), this.getZ() + dir.z * hop);
                }
                if (this.random.nextFloat() < 0.18F) pauseTimer = 100; // randomly stop ~5s
            }
        }
        if (this.distanceTo(player) < 1.8D) { killPlayer(player); this.discard(); return; }
        if (lifeTimer >= 2400) this.discard(); // 2 minutes
    }

    // stalker8 in front of a window.
    private void tickWindow(Player player) {
        this.getNavigation().stop();
        lifeTimer++;
        if (lifeTimer >= 1800) { this.discard(); return; } // always gone after ~90s
        if (player == null) return;
        faceTowards(player);
        if (staring) { if (--stareTimer <= 0) { applyDarkness(player, 8); this.discard(); } return; }
        if (isLookedAt(player, false)) { staring = true; stareTimer = 60; } // looks through glass
    }

    // stalker5 image behind the player + "Behind You" subtitle.
    private void tickBehind(Player player) {
        this.getNavigation().stop();
        if (player == null) { this.discard(); return; }
        if (!behindWarned) {
            behindWarned = true;
            onSpawnedWarn();
            if (isServerPlayer(player))
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new PacketHandler.BehindYouPacket(70));
        }
        faceTowards(player);
        behindTimer++;
        if (isLookedAt(player, false)) {
            if (this.random.nextFloat() < 0.5F) this.discard();
            else { applyDarkness(player, 5); this.discard(); }
            return;
        }
        if (behindTimer >= 200) this.discard(); // gone if never noticed
    }

    private void tickLock(Player player) {
        Player p = lockTarget != null ? lockTarget : player;
        this.getNavigation().stop();
        if (p == null) { this.discard(); return; }
        faceTowards(p);
        if (--lockTimer <= 0) { applyDarkness(p, 8); this.discard(); }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putInt("Mode", getMode());
        tag.putInt("LifeTimer", lifeTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        setMode(tag.getInt("Mode"));
        lifeTimer = tag.getInt("LifeTimer");
    }
}
