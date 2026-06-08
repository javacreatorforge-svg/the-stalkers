package com.redstonedev.thestalkers.entity;

import com.redstonedev.thestalkers.init.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GoatStalkerEntity extends AbstractStalker {

    // Variant: 1=goat1, 2=goat2, 3=goat3 (window), 4=goat4, 5=goatjumpscare (behind)
    private boolean staring = false;
    private int stareTimer = 0;
    private int behindTimer = 0;
    private int lifeTimer = 0;     // CHASE: 1-minute lifespan
    private boolean decided = false;
    private int warnCooldown = 0;
    private int ignoredTimer = 0;

    public GoatStalkerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ATTACK_DAMAGE, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 80.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide) return;
        if (warnCooldown > 0) warnCooldown--;

        int mode = getMode();
        Player player = nearestRealPlayer(80.0D);

        if (mode == MODE_WINDOW) { tickWindow(player); return; }
        if (mode == MODE_BEHIND) { tickBehind(player); return; }
        if (mode == MODE_CHASE)  { tickChase(player);  return; }

        // ---- MODE_STALK (not that much; stands still and watches) ----
        if (player == null) { this.getNavigation().stop(); return; }
        this.getNavigation().stop();
        faceTowards(player);

        if (staring) { if (--stareTimer <= 0) this.discard(); return; }

        // Get close to it while it stalks -> it vanishes.
        if (this.distanceTo(player) < 3.5D) { this.discard(); return; }

        // goatwarn plays while stalking (not during jumpscare modes).
        if (warnCooldown <= 0) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.GOAT_WARN.get(), SoundSource.HOSTILE, 0.9F, 1.0F);
            warnCooldown = 300 + this.random.nextInt(400);
        }

        if (isPlayerStaringAt(player)) {
            ignoredTimer = 0;
            if (!decided) {
                decided = true;
                if (this.random.nextFloat() < 0.5F) this.discard();
                else { staring = true; stareTimer = 80; } // stares back 4s
            }
        } else {
            ignoredTimer++;
            if (ignoredTimer >= 1800) this.discard();
        }
    }

    private void tickWindow(Player player) {
        this.getNavigation().stop();
        lifeTimer++;
        if (lifeTimer >= 1800) { this.discard(); return; } // always gone after ~90s
        if (player == null) return;
        faceTowards(player);
        if (staring) { if (--stareTimer <= 0) { applyDarkness(player, 8); this.discard(); } return; }
        if (isLookedAt(player, false)) { staring = true; stareTimer = 60; } // 3s, sees through glass
    }

    // goatjumpscare behind the player - silent (no goatwarn).
    private void tickBehind(Player player) {
        this.getNavigation().stop();
        if (player == null) { this.discard(); return; }
        faceTowards(player);
        if (staring) { if (--stareTimer <= 0) this.discard(); return; } // stares 2s then gone
        behindTimer++;
        if (isLookedAt(player, false)) { staring = true; stareTimer = 40; return; }
        if (behindTimer >= 600) this.discard(); // not looked at for 30s -> gone
    }

    // Rare fast chase.
    private void tickChase(Player player) {
        lifeTimer++;
        if (player == null) { if (lifeTimer >= 1200) this.discard(); return; }
        this.setTarget(player);
        this.getNavigation().moveTo(player, 1.6D);
        faceTowards(player);
        if (this.distanceTo(player) < 1.8D) { killPlayer(player); this.discard(); return; }
        if (lifeTimer >= 1200) this.discard(); // 1 minute, no kill
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
