package org.admany.taczt.platform.fabric.client;

import java.util.Map;

public final class AcceleratedMeshLookupCache {
   private static final int SIZE = 256;
   private static final ThreadLocal<Lookup> LOCAL = ThreadLocal.withInitial(Lookup::new);

   private AcceleratedMeshLookupCache() {
   }

   public static Object get(Map<?, ?> map, Object key) {
      Lookup lookup = (Lookup)LOCAL.get();
      int slot = slot(map, key);
      if (lookup.maps[slot] == map && lookup.keys[slot] == key) {
         Object cached = lookup.values[slot];
         if (cached != null) {
            return cached;
         }
      }

      Object value = map.get(key);
      if (value != null) {
         lookup.maps[slot] = map;
         lookup.keys[slot] = key;
         lookup.values[slot] = value;
      }

      return value;
   }

   public static Object put(Map<Object, Object> map, Object key, Object value) {
      Object previous = map.put(key, value);
      Lookup lookup = (Lookup)LOCAL.get();
      int slot = slot(map, key);
      lookup.maps[slot] = map;
      lookup.keys[slot] = key;
      lookup.values[slot] = value;
      return previous;
   }

   public static void clear() {
      LOCAL.remove();
   }

   private static int slot(Object map, Object key) {
      int hash = System.identityHashCode(map) * 31 + System.identityHashCode(key);
      hash ^= hash >>> 16;
      return hash & 255;
   }

   private static final class Lookup {
      private final Map<?, ?>[] maps = new Map[256];
      private final Object[] keys = new Object[256];
      private final Object[] values = new Object[256];
   }
}
