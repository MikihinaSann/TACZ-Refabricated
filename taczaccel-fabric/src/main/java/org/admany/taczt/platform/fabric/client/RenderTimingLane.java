package org.admany.taczt.platform.fabric.client;

import java.util.Arrays;

final class RenderTimingLane {
   private static final int CAPACITY = 65536;
   private final long[] samples = new long[65536];
   private int count;
   private int next;
   private long totalNanos;
   private long maximumNanos;

   void record(long started) {
      if (started != 0L) {
         long elapsed = Math.max(0L, System.nanoTime() - started);
         if (this.count == 65536) {
            long discarded = this.samples[this.next];
            this.totalNanos -= discarded;
            this.samples[this.next] = elapsed;
            this.totalNanos += elapsed;
            if (discarded == this.maximumNanos) {
               this.maximumNanos = elapsed;

               for(long sample : this.samples) {
                  this.maximumNanos = Math.max(this.maximumNanos, sample);
               }
            } else {
               this.maximumNanos = Math.max(this.maximumNanos, elapsed);
            }
         } else {
            this.samples[this.next] = elapsed;
            ++this.count;
            this.totalNanos += elapsed;
            this.maximumNanos = Math.max(this.maximumNanos, elapsed);
         }

         this.next = (this.next + 1) % 65536;
      }
   }

   Snapshot snapshotAndReset() {
      long[] copy = Arrays.copyOf(this.samples, this.count);
      Arrays.sort(copy);
      Snapshot snapshot = new Snapshot((long)this.count, this.totalNanos, this.maximumNanos, percentile(copy, (double)0.5F), percentile(copy, 0.95), percentile(copy, 0.99));
      this.count = 0;
      this.next = 0;
      this.totalNanos = 0L;
      this.maximumNanos = 0L;
      return snapshot;
   }

   private static double percentile(long[] values, double fraction) {
      if (values.length == 0) {
         return (double)0.0F;
      } else {
         int index = Math.min(values.length - 1, (int)Math.ceil((double)values.length * fraction) - 1);
         return (double)values[index] / (double)1000.0F;
      }
   }

   static record Snapshot(long renders, long totalNanos, long maximumNanos, double p50Micros, double p95Micros, double p99Micros) {
      double averageMicros() {
         return this.renders == 0L ? (double)0.0F : (double)this.totalNanos / (double)1000.0F / (double)this.renders;
      }

      double totalMillis() {
         return (double)this.totalNanos / (double)1000000.0F;
      }

      double maximumMicros() {
         return (double)this.maximumNanos / (double)1000.0F;
      }
   }
}
