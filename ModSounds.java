package com.argentinaflags;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Eventos de sonido de los discos. Los archivos .ogg (MONO) van en
 * assets/argentinaflags/sounds/music_disc/<id>.ogg. Los ids deben coincidir con tools/AssetGenerator.java.
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ArgentinaFlags.MOD_ID);

    public static final List<String> DISC_IDS = List.of(
            "himno_nacional",
            "marcha_san_lorenzo",
            "marcha_malvinas",
            "avenida_camelias",
            "aurora",
            "pucara_malvinas",
            "sobreviviendo",
            "el_grandote",
            "diablo_humahuaca",
            "campanas_noche",
            "cara_tramposo",
            "estrella_federal",
            "reina_madre"
    );

    public static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> DISC_SOUNDS = new LinkedHashMap<>();

    static {
        for (String id : DISC_IDS) {
            String name = "music_disc." + id;
            DISC_SOUNDS.put(id, SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ArgentinaFlags.MOD_ID, name))));
        }
    }

    private ModSounds() {
    }
}
