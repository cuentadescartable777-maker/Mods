package com.argentinaflags;

import com.argentinaflags.block.FlagBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registro de objetos: un BlockItem por bandera y el item "Sol de Mayo" usado en la receta. */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArgentinaFlags.MOD_ID);

    public static final DeferredItem<Item> SOL_DE_MAYO =
            ITEMS.register("sol_de_mayo", () -> new Item(new Item.Properties()));

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
