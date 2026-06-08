package com.redstonedev.thestalkers.init;

import com.redstonedev.thestalkers.TheStalkers;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TheStalkers.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> STALKER_SPAWN_EGG =
            ITEMS.register("stalker_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.STALKER, 0x101014, 0x550000,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<ForgeSpawnEggItem> GOAT_STALKER_SPAWN_EGG =
            ITEMS.register("goat_stalker_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.GOAT_STALKER, 0x2b2620, 0x885500,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
