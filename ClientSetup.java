package com.argentinaflags.client;

import com.argentinaflags.ArgentinaFlags;
import com.argentinaflags.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Registro de renderers (solo cliente). */
@EventBusSubscriber(modid = ArgentinaFlags.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FLAG.get(), FlagRenderer::new);
    }

    private ClientSetup() {
    }
}
