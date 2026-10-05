package com.argentinaflags;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Pestana "Argentina Flags" del modo creativo. */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArgentinaFlags.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ArgentinaFlags.MOD_ID + ".main"))
                    .icon(() -> new ItemStack(ModItems.FLAGS.get("flag_argentina").get()))
                    .displayItems((parameters, output) -> {
                        ModItems.FLAGS.values().forEach(item -> output.accept(item.get()));
                        output.accept(ModItems.FLAG_POLE.get());
                        output.accept(ModItems.SOL_DE_MAYO.get());
                        ModItems.DISCS.values().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
