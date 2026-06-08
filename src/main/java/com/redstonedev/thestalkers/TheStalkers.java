package com.redstonedev.thestalkers;

import com.mojang.logging.LogUtils;
import com.redstonedev.thestalkers.client.ClientSetup;
import com.redstonedev.thestalkers.entity.GoatStalkerEntity;
import com.redstonedev.thestalkers.entity.StalkerEntity;
import com.redstonedev.thestalkers.event.ForgeEvents;
import com.redstonedev.thestalkers.init.ModEntities;
import com.redstonedev.thestalkers.init.ModItems;
import com.redstonedev.thestalkers.init.ModSounds;
import com.redstonedev.thestalkers.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TheStalkers.MODID)
public class TheStalkers {
    public static final String MODID = "the_stalkers";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheStalkers() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::attributes);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        e.enqueueWork(PacketHandler::register);
        LOGGER.info("The Stalkers loaded. They are watching.");
    }
    private void clientSetup(final FMLClientSetupEvent e) { ClientSetup.onClientSetup(e); }
    private void attributes(final EntityAttributeCreationEvent e) {
        e.put(ModEntities.STALKER.get(), StalkerEntity.createAttributes().build());
        e.put(ModEntities.GOAT_STALKER.get(), GoatStalkerEntity.createAttributes().build());
    }
}
