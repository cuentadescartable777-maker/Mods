package com.argentinaflags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Eventos de sonido de los discos. Los archivos .ogg van en assets/argentinaflags/sounds/music_disc/. */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ArgentinaFlags.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HIMNO_NACIONAL = register("music_disc.himno_nacional");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARCHA_SAN_LORENZO = register("music_disc.marcha_san_lorenzo");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ArgentinaFlags.MOD_ID, name)));
    }

    private ModSounds() {
    }
}
