package me.drex.logblock.util;

import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public class EntityUtil {

    public static String toName(EntityType entityType) {
        return Registry.ENTITY_TYPE.getId(entityType).getPath();
    }

}
