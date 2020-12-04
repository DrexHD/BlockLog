package me.drex.logblock.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class BlockUtil {

    public static <T extends Comparable<T>, V extends T> BlockState fromString(Block block, String string) {
        BlockState blockState = block.getDefaultState();
        JsonArray jsonArray = new JsonParser().parse(string).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = (JsonObject) jsonArray.get(i);
            for (Property<?> property : blockState.getProperties()) {
                Property<T> p = (Property<T>) property;
                JsonElement jsonElement = jsonObject.get(property.getName());
                if (jsonElement != null) {
                    for (Comparable<?> value : property.getValues()) {
                        V v = (V) value;
                        if (value.toString().equals(jsonElement.toString().substring(1, jsonElement.toString().length() - 1))) {
                            blockState = blockState.with(p, v);
                        }
                    }
                }
            }
        }
        return blockState;
    }

    public static String toJsonString(BlockState blockState) {
        JsonArray jsonArray = new JsonArray();
        blockState.getProperties().forEach(property -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(property.getName(), blockState.get(property).toString());
            jsonArray.add(jsonObject);
        });
        return jsonArray.toString();
    }

    public static String toNameSpace(Block block) {
        return Registry.BLOCK.getId(block).getNamespace() + ":" + Registry.BLOCK.getId(block).getPath();
    }

    public static String toName(Block block) {
        return Registry.BLOCK.getId(block).getPath();
    }

    public static CompoundTag getTagAt(World world, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) tag = blockEntity.toTag(tag);
        return tag;
    }

    public static CompoundTag toTag(byte[] bytes) {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

}
