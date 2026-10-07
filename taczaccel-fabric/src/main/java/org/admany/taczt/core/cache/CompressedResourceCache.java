package org.admany.taczt.core.cache;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.admany.quantified.api.compute.QuantifiedCompute;
import org.admany.quantified.api.compute.QuantifiedCompute.Complexity;
import org.admany.quantified.api.compute.QuantifiedCompute.WorkloadKind;

public final class CompressedResourceCache {
   private static final int PAGE_BYTES = 32768;
   private static final long MAX_RETAINED_BYTES = 33554432L;
   private static final long MAX_IN_FLIGHT_BYTES = 8388608L;
   private static final int MAX_ENTRIES = 2048;
   private static final AtomicLong RETAINED_BYTES = new AtomicLong();
   private static final AtomicLong IN_FLIGHT_BYTES = new AtomicLong();
   private static final AtomicLong TASK_SEQUENCE = new AtomicLong();
   private static final ConcurrentHashMap<Integer, AtomicLong> GENERATIONS = new ConcurrentHashMap();
   private static final ThreadLocal<ReadContext> CURRENT_READ = new ThreadLocal();
   private static final ThreadLocal<byte[]> DECOMPRESSION_BUFFER = ThreadLocal.withInitial(() -> new byte['耀']);
   private static final Map<CacheKey, Entry> ENTRIES = new LinkedHashMap(128, 0.75F, true);

   private CompressedResourceCache() {
   }

   public static void beginGeneration(Object owner) {
      int ownerId = System.identityHashCode(owner);
      long generation = ((AtomicLong)GENERATIONS.computeIfAbsent(ownerId, (ignored) -> new AtomicLong())).incrementAndGet();
      synchronized(ENTRIES) {
         ENTRIES.entrySet().removeIf((entry) -> {
            if (((CacheKey)entry.getKey()).ownerId != ownerId) {
               return false;
            } else {
               RETAINED_BYTES.addAndGet((long)(-((Entry)entry.getValue()).compressedBytes));
               return true;
            }
         });
      }
   }

   public static void beginRead(Object owner, ResourceLocation resource) {
      int ownerId = System.identityHashCode(owner);
      long generation = ((AtomicLong)GENERATIONS.computeIfAbsent(ownerId, (ignored) -> new AtomicLong())).get();
      CURRENT_READ.set(new ReadContext(ownerId, generation, resource));
   }

   public static void endRead() {
      CURRENT_READ.remove();
   }

   public static BufferedReader openReader(Resource resource) throws IOException {
      ReadContext context = (ReadContext)CURRENT_READ.get();
      if (context == null) {
         return resource.openAsReader();
      } else {
         CacheKey key = new CacheKey(context.ownerId, context.generation, context.resource);
         Entry cached;
         synchronized(ENTRIES) {
            cached = (Entry)ENTRIES.get(key);
         }

         if (cached != null) {
            try {
               return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cached.materialize()), StandardCharsets.UTF_8));
            } catch (IOException var11) {
               evict(key, cached);
            }
         }

         InputStream input = resource.open();

         byte[] bytes;
         try {
            bytes = input.readAllBytes();
         } catch (Throwable var10) {
            if (input != null) {
               try {
                  input.close();
               } catch (Throwable var8) {
                  var10.addSuppressed(var8);
               }
            }

            throw var10;
         }

         if (input != null) {
            input.close();
         }

         scheduleCompression(key, bytes);
         return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
      }
   }

   private static void scheduleCompression(CacheKey key, byte[] bytes) {
      if (bytes.length != 0 && (long)bytes.length <= 33554432L) {
         if (IN_FLIGHT_BYTES.addAndGet((long)bytes.length) > 8388608L) {
            IN_FLIGHT_BYTES.addAndGet((long)(-bytes.length));
         } else {
            QuantifiedCompute.builder("taczaccelerated", "compressed-gunpack-resource", TASK_SEQUENCE.incrementAndGet()).cpuFallback(() -> CompressedResourceCache.Entry.compress(bytes)).dataSizeBytes((long)bytes.length).parallelUnits(1).complexity(Complexity.MODERATE).kind(WorkloadKind.GENERAL).cpuOnly().allowMainThreadRerouting(false).submit().whenComplete((entry, failure) -> {
               IN_FLIGHT_BYTES.addAndGet((long)(-bytes.length));
               if (failure == null && entry instanceof Entry compressed && generationIsCurrent(key)) {
                  publish(key, compressed);
               }

            });
         }
      }
   }

   private static boolean generationIsCurrent(CacheKey key) {
      AtomicLong generation = (AtomicLong)GENERATIONS.get(key.ownerId);
      return generation != null && generation.get() == key.generation;
   }

   private static void publish(CacheKey key, Entry entry) {
      synchronized(ENTRIES) {
         Entry old = (Entry)ENTRIES.put(key, entry);
         if (old != null) {
            RETAINED_BYTES.addAndGet((long)(-old.compressedBytes));
         }

         RETAINED_BYTES.addAndGet((long)entry.compressedBytes);

         while(ENTRIES.size() > 2048 || RETAINED_BYTES.get() > 33554432L) {
            Map.Entry<CacheKey, Entry> eldest = (Map.Entry)ENTRIES.entrySet().iterator().next();
            ENTRIES.remove(eldest.getKey());
            RETAINED_BYTES.addAndGet((long)(-((Entry)eldest.getValue()).compressedBytes));
         }

      }
   }

   private static void evict(CacheKey key, Entry expected) {
      synchronized(ENTRIES) {
         if (ENTRIES.remove(key, expected)) {
            RETAINED_BYTES.addAndGet((long)(-expected.compressedBytes));
         }

      }
   }

   private static record ReadContext(int ownerId, long generation, ResourceLocation resource) {
   }

   private static record CacheKey(int ownerId, long generation, ResourceLocation resource) {
   }

   static final class Entry {
      private final byte[][] pages;
      private final int uncompressedBytes;
      private final int compressedBytes;

      private Entry(byte[][] pages, int uncompressedBytes, int compressedBytes) {
         this.pages = pages;
         this.uncompressedBytes = uncompressedBytes;
         this.compressedBytes = compressedBytes;
      }

      static Entry compress(byte[] source) {
         int count = (source.length + '耀' - 1) / '耀';
         byte[][] pages = new byte[count][];
         int stored = 0;

         for(int page = 0; page < count; ++page) {
            int offset = page * '耀';
            int length = Math.min(32768, source.length - offset);
            ByteArrayOutputStream output = new ByteArrayOutputStream(length);

            try {
               DeflaterOutputStream deflater = new DeflaterOutputStream(output, new Deflater(1), 32768, true);

               try {
                  deflater.write(source, offset, length);
               } catch (Throwable var12) {
                  try {
                     deflater.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }

                  throw var12;
               }

               deflater.close();
            } catch (IOException exception) {
               throw new IllegalStateException("Failed to compress detached TACZ resource bytes", exception);
            }

            pages[page] = output.toByteArray();
            stored += pages[page].length;
         }

         return new Entry(pages, source.length, stored);
      }

      byte[] materialize() throws IOException {
         ByteArrayOutputStream output = new ByteArrayOutputStream(this.uncompressedBytes);
         byte[] buffer = (byte[])CompressedResourceCache.DECOMPRESSION_BUFFER.get();

         for(byte[] page : this.pages) {
            InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(page));

            int read;
            try {
               while((read = inflater.read(buffer)) >= 0) {
                  output.write(buffer, 0, read);
               }
            } catch (Throwable var11) {
               try {
                  inflater.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            inflater.close();
         }

         byte[] bytes = output.toByteArray();
         if (bytes.length != this.uncompressedBytes) {
            throw new IOException("Compressed TACZ resource size mismatch");
         } else {
            return bytes;
         }
      }
   }
}
