package org.admany.taczt.platform.fabric.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GunRenderProfiler {
   private static final RenderTimingLane FIRST_PERSON = new RenderTimingLane();
   private static final RenderTimingLane ITEM = new RenderTimingLane();
   private static final RenderTimingLane ANIMATION = new RenderTimingLane();
   private static final RenderTimingLane TRANSFORM = new RenderTimingLane();
   private static final RenderTimingLane MODEL = new RenderTimingLane();
   private static final RenderTimingLane MODEL_GEOMETRY = new RenderTimingLane();
   private static final RenderTimingLane MODEL_FLUSH = new RenderTimingLane();
   private static final RenderTimingLane MODEL_DELEGATES = new RenderTimingLane();
   private static final RenderTimingLane CUBE_VANILLA = new RenderTimingLane();
   private static final RenderTimingLane CUBE_SCALAR = new RenderTimingLane();
   private static final RenderTimingLane ATTACHMENT_MODEL = new RenderTimingLane();
   private static final RenderTimingLane ATTACHMENT_FALLBACK = new RenderTimingLane();
   private static final RenderTimingLane ATTACHMENT_DEFERRED = new RenderTimingLane();
   private static final RenderTimingLane FUNCTIONAL_ATTACHMENT = new RenderTimingLane();
   private static final RenderTimingLane FUNCTIONAL_HAND = new RenderTimingLane();
   private static final RenderTimingLane FUNCTIONAL_TEXT = new RenderTimingLane();
   private static final RenderTimingLane FUNCTIONAL_EFFECT = new RenderTimingLane();
   private static final RenderTimingLane FUNCTIONAL_OTHER = new RenderTimingLane();
   private static final Map<String, RenderTimingLane> OTHER_BY_CLASS = new LinkedHashMap();

   private GunRenderProfiler() {
   }

   public static long begin() {
      return TacztClientBenchmark.isProfilingActive() ? System.nanoTime() : 0L;
   }

   public static void endFirstPerson(long started) {
      FIRST_PERSON.record(started);
   }

   public static void endItem(long started) {
      ITEM.record(started);
   }

   public static void endAnimation(long started) {
      ANIMATION.record(started);
   }

   public static void endTransform(long started) {
      TRANSFORM.record(started);
   }

   public static void endModel(long started) {
      MODEL.record(started);
   }

   public static void endModelGeometry(long started) {
      MODEL_GEOMETRY.record(started);
   }

   public static void endModelFlush(long started) {
      MODEL_FLUSH.record(started);
   }

   public static void endModelDelegates(long started) {
      MODEL_DELEGATES.record(started);
   }

   public static void endCubeVanilla(long started) {
      CUBE_VANILLA.record(started);
   }

   public static void endCubeScalar(long started) {
      CUBE_SCALAR.record(started);
   }

   public static void endAttachmentModel(long started) {
      ATTACHMENT_MODEL.record(started);
   }

   public static void endAttachmentFallback(long started) {
      ATTACHMENT_FALLBACK.record(started);
   }

   public static void endAttachmentDeferred(long started) {
      ATTACHMENT_DEFERRED.record(started);
   }

   public static void endFunctionalAttachment(long started) {
      FUNCTIONAL_ATTACHMENT.record(started);
   }

   public static void endFunctionalHand(long started) {
      FUNCTIONAL_HAND.record(started);
   }

   public static void endFunctionalText(long started) {
      FUNCTIONAL_TEXT.record(started);
   }

   public static void endFunctionalEffect(long started) {
      FUNCTIONAL_EFFECT.record(started);
   }

   public static void endFunctionalOther(long started) {
      FUNCTIONAL_OTHER.record(started);
   }

   public static void endFunctionalOther(long started, String className) {
      FUNCTIONAL_OTHER.record(started);
      if (started != 0L) {
         RenderTimingLane lane = (RenderTimingLane)OTHER_BY_CLASS.get(className);
         if (lane == null && OTHER_BY_CLASS.size() < 16) {
            lane = new RenderTimingLane();
            OTHER_BY_CLASS.put(className, lane);
         }

         if (lane != null) {
            lane.record(started);
         }

      }
   }

   public static Snapshot snapshotFirstPersonAndReset() {
      return snapshot(FIRST_PERSON);
   }

   public static Snapshot snapshotItemAndReset() {
      return snapshot(ITEM);
   }

   public static Snapshot snapshotAnimationAndReset() {
      return snapshot(ANIMATION);
   }

   public static Snapshot snapshotTransformAndReset() {
      return snapshot(TRANSFORM);
   }

   public static Snapshot snapshotModelAndReset() {
      return snapshot(MODEL);
   }

   public static Snapshot snapshotModelGeometryAndReset() {
      return snapshot(MODEL_GEOMETRY);
   }

   public static Snapshot snapshotModelFlushAndReset() {
      return snapshot(MODEL_FLUSH);
   }

   public static Snapshot snapshotModelDelegatesAndReset() {
      return snapshot(MODEL_DELEGATES);
   }

   public static Snapshot snapshotCubeVanillaAndReset() {
      return snapshot(CUBE_VANILLA);
   }

   public static Snapshot snapshotCubeScalarAndReset() {
      return snapshot(CUBE_SCALAR);
   }

   public static Snapshot snapshotAttachmentModelAndReset() {
      return snapshot(ATTACHMENT_MODEL);
   }

   public static Snapshot snapshotAttachmentFallbackAndReset() {
      return snapshot(ATTACHMENT_FALLBACK);
   }

   public static Snapshot snapshotAttachmentDeferredAndReset() {
      return snapshot(ATTACHMENT_DEFERRED);
   }

   public static Snapshot snapshotFunctionalAttachmentAndReset() {
      return snapshot(FUNCTIONAL_ATTACHMENT);
   }

   public static Snapshot snapshotFunctionalHandAndReset() {
      return snapshot(FUNCTIONAL_HAND);
   }

   public static Snapshot snapshotFunctionalTextAndReset() {
      return snapshot(FUNCTIONAL_TEXT);
   }

   public static Snapshot snapshotFunctionalEffectAndReset() {
      return snapshot(FUNCTIONAL_EFFECT);
   }

   public static Snapshot snapshotFunctionalOtherAndReset() {
      return snapshot(FUNCTIONAL_OTHER);
   }

   public static List<ClassSnapshot> snapshotOtherClassesAndReset() {
      List<ClassSnapshot> snapshots = new ArrayList(OTHER_BY_CLASS.size());

      for(Map.Entry<String, RenderTimingLane> entry : OTHER_BY_CLASS.entrySet()) {
         snapshots.add(new ClassSnapshot((String)entry.getKey(), snapshot((RenderTimingLane)entry.getValue())));
      }

      OTHER_BY_CLASS.clear();
      snapshots.sort(Comparator.comparingDouble((ClassSnapshot snapshot) -> snapshot.timing().totalMillis()).reversed());
      return snapshots;
   }

   private static Snapshot snapshot(RenderTimingLane lane) {
      RenderTimingLane.Snapshot snapshot = lane.snapshotAndReset();
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

   public static record ClassSnapshot(String className, Snapshot timing) {
   }
}
