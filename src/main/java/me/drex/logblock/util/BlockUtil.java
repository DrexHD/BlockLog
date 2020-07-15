package me.drex.logblock.util;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

public class BlockUtil {

    public static String toName(Block block) {
        return Registry.BLOCK.getId(block).getNamespace() + ":" + Registry.BLOCK.getId(block).getPath();
    }

}
