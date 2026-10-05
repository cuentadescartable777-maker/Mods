package com.argentinaflags.block;

import com.argentinaflags.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Block entity sin datos ni tick: existe solo para que el cliente pueda dibujar la tela con un renderer.
 * La posicion, orientacion y tamano salen del BlockState, que Minecraft ya sincroniza.
 */
public class FlagBlockEntity extends BlockEntity {

    public FlagBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLAG.get(), pos, state);
    }

    /** La tela se extiende mas alla del bloque: ampliamos la caja usada para decidir si se dibuja. */
    public AABB getRenderBoundingBox() {
        boolean large = getBlockState().getBlock() instanceof FlagBlock flag && flag.isLarge();
        double r = large ? 4.0 : 2.0;
        return new AABB(getBlockPos()).inflate(r, r, r);
    }
}
