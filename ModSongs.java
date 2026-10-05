package com.argentinaflags;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.JukeboxSong;

/** Claves de las canciones (definidas en data/argentinaflags/jukebox_song/*.json). */
public final class ModSongs {

    public static final Map<String, ResourceKey<JukeboxSong>> KEYS = new LinkedHashMap<>();

    static {
        for (String id : ModSounds.DISC_IDS) {
            KEYS.put(id, ResourceKey.create(Registries.JUKEBOX_SONG,
                    ResourceLocation.fromNamespaceAndPath(ArgentinaFlags.MOD_ID, id)));
        }
    }

    private ModSongs() {
    }
}
