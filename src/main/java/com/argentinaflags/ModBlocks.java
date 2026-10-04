package com.argentinaflags;

import com.argentinaflags.block.FlagBlock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registro de bloques. Para agregar una bandera, sumar su id a FLAG_IDS (y a tools/AssetGenerator.java). */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArgentinaFlags.MOD_ID);

    /** Ids de todas las banderas (deben coincidir con los ids de AssetGenerator). */
    public static final List<String> FLAG_IDS = List.of(
            "flag_argentina",
            "flag_argentina_sin_sol",
            "flag_confederacion",
            "flag_liga_federal",
            "flag_buenos_aires",
            "flag_cordoba",
            "flag_santa_fe",
            "flag_mendoza",
            "flag_entre_rios",
            "flag_patagonia"
    );

    public static final Map<String, DeferredBlock<FlagBlock>> FLAGS = new LinkedHashMap<>();

    static {
        for (String id : FLAG_IDS) {
            FLAGS.put(id, BLOCKS.registerBlock(id, FlagBlock::new, FlagBlock.defaultProperties()));
        }
    }

    private ModBlocks() {
    }
}
