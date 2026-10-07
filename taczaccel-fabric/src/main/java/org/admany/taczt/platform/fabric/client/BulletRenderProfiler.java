package org.admany.taczt.platform.fabric.client;

public final class BulletRenderProfiler {
   private static final RenderTimingLane LANE = new RenderTimingLane();

   private BulletRenderProfiler() {
   }

   public static long begin() {
      return TacztClientBenchmark.isProfilingActive() ? System.nanoTime() : 0L;
   }

   public static void end(long started) {
      LANE.record(started);
   }

   public static Snapshot snapshotAndReset() {
      RenderTimingLane.Snapshot snapshot = LANE.snapshotAndReset();
      return new Snapshot(snapshot.renders(), snapshot.totalNanos(), snapshot.maximumNanos(), snapshot.p50Micros(), snapshot.p95Micros(), snapshot.p99Micros());
   }

   public static record Snapshot(long renders, long totalNanos, long maximumNanos, double p50Micros, double p95Micros, double p99Micros) {
      public double averageMicros() {
         return this.renders == 0L ? (double)0.0F : (double)this.totalNanos / (double)1000.0F / (double)this.renders;
      }

      public double totalMillis() {
         return (double)this.totalNanos / (double)1000000.0F;
      }

      public double maximumMicros() {
         return (double)this.maximumNanos / (double)1000.0F;
      }
   }
}
