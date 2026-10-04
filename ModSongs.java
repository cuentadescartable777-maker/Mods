package com.argentinaflags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.JukeboxSong;

/** Claves de las canciones (definidas en data/argentinaflags/jukebox_song/*.json). */
public final class ModSongs {

    public static final ResourceKey<JukeboxSong> HIMNO_NACIONAL = key("himno_nacional");
    public static final ResourceKey<JukeboxSong> MARCHA_SAN_LORENZO = key("marcha_san_lorenzo");

    private static ResourceKey<JukeboxSong> key(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG,
                ResourceLocation.fromNamespaceAndPath(ArgentinaFlags.MOD_ID, name));
    }

    private ModSongs() {
    }
}
