package com.argentinaflags;

import com.argentinaflags.block.FlagBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block entity de las banderas: no guarda datos ni hace tick; solo permite dibujar la tela con un renderer. */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ArgentinaFlags.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlagBlockEntity>> FLAG =
            BLOCK_ENTITIES.register("flag", () -> BlockEntityType.Builder.of(
                    FlagBlockEntity::new,
                    ModBlocks.FLAGS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));

    private ModBlockEntities() {
    }
}
