package com.tacz.guns.resource.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 物品堆叠 JSON 兼容解析器<br/>
 * 同时支持新版格式 {"id": "namespace:item", "count": n, "components": {...}}
 * 以及旧版格式 {"item": "namespace:item", "count": n, "nbt": {...}}<br/>
 * 旧版的 nbt 会被写入 {@link DataComponents#CUSTOM_DATA} 组件
 */
public final class ItemStackParser {
    private ItemStackParser() {
    }

    public static ItemStack parse(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return ItemStack.EMPTY;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return ofLegacy(element.getAsString(), 1, null);
        }
        if (!element.isJsonObject()) {
            throw new JsonParseException("Expected " + element + " to be a ItemStack");
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("id")) {
            return ItemStack.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow();
        }
        if (object.has("item")) {
            String itemId = GsonHelper.getAsString(object, "item");
            int count = GsonHelper.getAsInt(object, "count", 1);
            CompoundTag nbt = object.has("nbt") ? parseNbt(object.get("nbt")) : null;
            return ofLegacy(itemId, count, nbt);
        }
        throw new JsonParseException("Expected " + element + " to have a 'id' or 'item' member");
    }

    private static ItemStack ofLegacy(String itemId, int count, CompoundTag nbt) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        ItemStack stack = new ItemStack(item, Math.max(count, 1));
        if (nbt != null && !stack.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
        return stack;
    }

    private static CompoundTag parseNbt(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            try {
                return TagParser.parseTag(element.getAsString());
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid nbt: " + element, e);
            }
        }
        return CompoundTag.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
    }
}
