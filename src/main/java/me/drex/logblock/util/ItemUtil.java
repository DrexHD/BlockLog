package me.drex.logblock.util;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class ItemUtil {

    public static String toName(Item item) {
        return Registry.ITEM.getId(item).getNamespace() + ":" + Registry.ITEM.getId(item).getPath();
    }

}
