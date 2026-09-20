package com.tacz.guns.client.resource.serialize;

import com.google.gson.*;
import com.tacz.guns.resource.serialize.ItemStackParser;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;

public class ItemStackSerializer implements JsonDeserializer<ItemStack> {
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return ItemStackParser.parse(json);
    }
}
