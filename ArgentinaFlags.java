package com.argentinaflags;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clase principal del mod Argentina Flags. */
@Mod(ArgentinaFlags.MOD_ID)
public class ArgentinaFlags {

    public static final String MOD_ID = "argentinaflags";

    public ArgentinaFlags(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
    }
}
