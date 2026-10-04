package com.argentinaflags;

import com.argentinaflags.block.FlagBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registro de objetos: un BlockItem por bandera y el item "Sol de Mayo" usado en la receta. */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArgentinaFlags.MOD_ID);

    public static final DeferredItem<Item> SOL_DE_MAYO =
            ITEMS.register("sol_de_mayo", () -> new Item(new Item.Properties()));

    public static final DeferredItem<BlockItem> FLAG_POLE =
            ITEMS.register("flag_pole", () -> new BlockItem(ModBlocks.FLAG_POLE.get(), new Item.Properties()));

    public static final DeferredItem<Item> MUSIC_DISC_HIMNO_NACIONAL = ITEMS.register("music_disc_himno_nacional",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
                    .jukeboxPlayable(ModSongs.HIMNO_NACIONAL)));

    public static final DeferredItem<Item> MUSIC_DISC_MARCHA_SAN_LORENZO = ITEMS.register("music_disc_marcha_san_lorenzo",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
                    .jukeboxPlayable(ModSongs.MARCHA_SAN_LORENZO)));

    public static final Map<String, DeferredItem<BlockItem>> FLAGS = new LinkedHashMap<>();

    static {
        for (Map.Entry<String, DeferredBlock<FlagBlock>> entry : ModBlocks.FLAGS.entrySet()) {
            DeferredBlock<FlagBlock> block = entry.getValue();
            FLAGS.put(entry.getKey(), ITEMS.register(entry.getKey(), () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    private ModItems() {
    }
}
