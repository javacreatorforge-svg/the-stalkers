package com.redstonedev.thestalkers.init;

import com.redstonedev.thestalkers.TheStalkers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TheStalkers.MODID);

    public static final RegistryObject<SoundEvent> STALKER_WARN = register("stalker_warn");
    public static final RegistryObject<SoundEvent> GOAT_WARN     = register("goat_warn");
    public static final RegistryObject<SoundEvent> KNOCK         = register("knock");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(TheStalkers.MODID, name)));
    }
}
