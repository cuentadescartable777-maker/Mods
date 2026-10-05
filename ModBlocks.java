package com.argentinaflags;

import com.argentinaflags.block.FlagBlock;
import com.argentinaflags.block.PoleBlock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro de bloques. Para agregar una bandera: sumar su id a BASE_FLAG_IDS (y a tools/AssetGenerator.java).
 * Cada id genera dos bloques: el normal (flag_x) y el grande (flag_x_grande, 4 veces el area).
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArgentinaFlags.MOD_ID);

    public static final String LARGE_SUFFIX = "_grande";

    /** Ids base de todas las banderas (deben coincidir con los ids de AssetGenerator). */
    public static final List<String> BASE_FLAG_IDS = List.of(
            "flag_argentina",
            "flag_argentina_sin_sol",
            "flag_confederacion",
            "flag_confederacion_rosas",
            "flag_liga_federal",
            "flag_buenos_aires",
            "flag_cordoba",
            "flag_santa_fe",
            "flag_mendoza",
            "flag_entre_rios",
            "flag_patagonia",
            "flag_caba",
            "flag_canuelas",
            "flag_jujuy",
            "flag_salta",
            "flag_formosa",
            "flag_misiones",
            "flag_corrientes",
            "flag_chaco",
            "flag_santiago_del_estero",
            "flag_tucuman",
            "flag_catamarca",
            "flag_la_rioja",
            "flag_san_juan",
            "flag_san_luis",
            "flag_la_pampa",
            "flag_rio_negro",
            "flag_chubut",
            "flag_santa_cruz",
            "flag_tierra_del_fuego",
            "flag_malvinas"
    );

    /** Todos los ids de bandera: normales y grandes (62). */
    public static final List<String> FLAG_IDS = allFlagIds();

    /** Segmento de mastil apilable: permite subir la bandera. */
    public static final DeferredBlock<PoleBlock> FLAG_POLE =
            BLOCKS.registerBlock("flag_pole", PoleBlock::new, PoleBlock.defaultProperties());

    public static final Map<String, DeferredBlock<FlagBlock>> FLAGS = new LinkedHashMap<>();

    static {
        for (String baseId : BASE_FLAG_IDS) {
            FLAGS.put(baseId, BLOCKS.registerBlock(baseId,
                    properties -> new FlagBlock(properties, baseId, false), FlagBlock.defaultProperties()));
            String largeId = baseId + LARGE_SUFFIX;
            FLAGS.put(largeId, BLOCKS.registerBlock(largeId,
                    properties -> new FlagBlock(properties, baseId, true), FlagBlock.defaultProperties()));
        }
    }

    private static List<String> allFlagIds() {
        List<String> ids = new ArrayList<>();
        for (String baseId : BASE_FLAG_IDS) {
            ids.add(baseId);
            ids.add(baseId + LARGE_SUFFIX);
        }
        return List.copyOf(ids);
    }

    private ModBlocks() {
    }
}
