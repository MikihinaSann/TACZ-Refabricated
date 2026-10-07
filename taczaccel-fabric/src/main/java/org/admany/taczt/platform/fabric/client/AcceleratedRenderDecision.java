package org.admany.taczt.platform.fabric.client;

public final class AcceleratedRenderDecision {
   private static final ThreadLocal<State> LOCAL = ThreadLocal.withInitial(State::new);
   private static volatile long epoch;

   private AcceleratedRenderDecision() {
   }

   public static boolean shouldAccelerate() {
      State state = (State)LOCAL.get();
      long currentEpoch = epoch;
      if (state.epoch != currentEpoch) {
         state.epoch = currentEpoch;
         state.value = AcceleratedRenderingCompat.shouldAccelerate();
      }

      return state.value;
   }

   public static void invalidate() {
      ++epoch;
   }

   public static void clear() {
      LOCAL.remove();
   }

   private static final class State {
      private long epoch = Long.MIN_VALUE;
      private boolean value;
   }
}
