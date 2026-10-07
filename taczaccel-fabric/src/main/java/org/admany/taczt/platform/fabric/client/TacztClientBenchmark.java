package org.admany.taczt.platform.fabric.client;

import java.util.Arrays;
import java.util.Locale;

public final class TacztClientBenchmark {
   private static final int MAX_SAMPLES = 36000;
   private static final long CAPTURE_NANOS = 30000000000L;
   private static final long[] FRAME_NANOS = new long['負'];
   private static volatile boolean running;
   private static long renderStartNanos;
   private static long deadlineNanos;
   private static volatile Summary completedSummary;
   private static int samples;
   private static int writeIndex;

   private TacztClientBenchmark() {
   }

   public static synchronized void start() {
      running = true;
      renderStartNanos = 0L;
      samples = 0;
      writeIndex = 0;
      deadlineNanos = System.nanoTime() + 30000000000L;
      completedSummary = null;
   }

   public static synchronized void stop() {
      running = false;
      renderStartNanos = 0L;
      deadlineNanos = 0L;
   }

   public static boolean isRunning() {
      return running;
   }

   public static boolean isProfilingActive() {
      return running;
   }

   public static void beginFrame() {
      if (running) {
         renderStartNanos = System.nanoTime();
      }
   }

   public static void endFrame() {
      if (running) {
         long started = renderStartNanos;
         if (started != 0L) {
            FRAME_NANOS[writeIndex] = System.nanoTime() - started;
            writeIndex = (writeIndex + 1) % '負';
            if (samples < 36000) {
               ++samples;
            }

            renderStartNanos = 0L;
            if (System.nanoTime() >= deadlineNanos) {
               running = false;
               deadlineNanos = 0L;
               completedSummary = summary();
            }

         }
      }
   }

   public static Summary consumeCompletedSummary() {
      Summary result = completedSummary;
      completedSummary = null;
      return result;
   }

   public static int captureSeconds() {
      return 30;
   }

   public static Summary summary() {
      if (samples == 0) {
         return TacztClientBenchmark.Summary.empty();
      } else {
         long[] values = new long[samples];
         if (samples < 36000) {
            System.arraycopy(FRAME_NANOS, 0, values, 0, samples);
         } else {
            int tail = '負' - writeIndex;
            System.arraycopy(FRAME_NANOS, writeIndex, values, 0, tail);
            System.arraycopy(FRAME_NANOS, 0, values, tail, writeIndex);
         }

         long total = 0L;
         int above16 = 0;
         int above33 = 0;
         int above50 = 0;
         int above100 = 0;

         for(long value : values) {
            total += value;
            if (value > 16670000L) {
               ++above16;
            }

            if (value > 33330000L) {
               ++above33;
            }

            if (value > 50000000L) {
               ++above50;
            }

            if (value > 100000000L) {
               ++above100;
            }
         }

         Arrays.sort(values);
         return new Summary(samples, (double)total / (double)1000000.0F / (double)samples, percentile(values, (double)0.5F), percentile(values, 0.95), percentile(values, 0.99), (double)values[values.length - 1] / (double)1000000.0F, above16, above33, above50, above100);
      }
   }

   private static double percentile(long[] values, double percentile) {
      int index = Math.min(values.length - 1, (int)Math.ceil(percentile * (double)values.length) - 1);
      return (double)values[index] / (double)1000000.0F;
   }

   public static record Summary(int samples, double meanMillis, double p50Millis, double p95Millis, double p99Millis, double maxMillis, int above16Millis, int above33Millis, int above50Millis, int above100Millis) {
      private static Summary empty() {
         return new Summary(0, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, 0, 0, 0, 0);
      }

      public String format() {
         return String.format(Locale.ROOT, "%d frames captured. Average %.3fms. Median %.3fms. P95 %.3fms. P99 %.3fms. Worst frame %.3fms. Frames over 16.67ms: %d. Over 33.33ms: %d. Over 50ms: %d. Over 100ms: %d.", this.samples, this.meanMillis, this.p50Millis, this.p95Millis, this.p99Millis, this.maxMillis, this.above16Millis, this.above33Millis, this.above50Millis, this.above100Millis);
      }
   }
}
