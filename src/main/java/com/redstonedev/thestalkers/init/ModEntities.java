package com.redstonedev.thestalkers.init;

import com.redstonedev.thestalkers.TheStalkers;
import com.redstonedev.thestalkers.entity.GoatStalkerEntity;
import com.redstonedev.thestalkers.entity.StalkerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TheStalkers.MODID);

    public static final RegistryObject<EntityType<StalkerEntity>> STALKER =
            ENTITIES.register("stalker", () -> EntityType.Builder
                    .<StalkerEntity>of(StalkerEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 3.35F).clientTrackingRange(24).build(
                            new ResourceLocation(TheStalkers.MODID, "stalker").toString()));

    public static final RegistryObject<EntityType<GoatStalkerEntity>> GOAT_STALKER =
            ENTITIES.register("goat_stalker", () -> EntityType.Builder
                    .<GoatStalkerEntity>of(GoatStalkerEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.44F).clientTrackingRange(24).build(
                            new ResourceLocation(TheStalkers.MODID, "goat_stalker").toString()));
}
