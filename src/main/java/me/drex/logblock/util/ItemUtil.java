package me.drex.logblock.util;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class ItemUtil {

    public static String toName(Item item) {
        return Registry.ITEM.getId(item).getNamespace() + ":" + Registry.ITEM.getId(item).getPath();
    }

    public static Block toBlock(BlockItem item) {
        return item.getBlock();
    }

}
