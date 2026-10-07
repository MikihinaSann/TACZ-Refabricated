package org.admany.taczt.platform.fabric.client;

import com.mojang.math.Axis;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import org.joml.Quaternionf;

public final class BedrockRotationPool {
   private static final int MAX_RETAINED = 64;
   private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

   private BedrockRotationPool() {
   }

   public static Quaternionf borrow(Axis axis, float angle) {
      State state = (State)STATE.get();
      if (axis != Axis.XP && axis != Axis.XN && axis != Axis.YP && axis != Axis.YN && axis != Axis.ZP && axis != Axis.ZN) {
         return axis.rotation(angle);
      } else {
         Quaternionf rotation = (Quaternionf)state.available.pollFirst();
         if (rotation == null) {
            rotation = new Quaternionf();
         }

         if (axis == Axis.XP) {
            rotation.rotationX(angle);
         } else if (axis == Axis.XN) {
            rotation.rotationX(-angle);
         } else if (axis == Axis.YP) {
            rotation.rotationY(angle);
         } else if (axis == Axis.YN) {
            rotation.rotationY(-angle);
         } else if (axis == Axis.ZP) {
            rotation.rotationZ(angle);
         } else if (axis == Axis.ZN) {
            rotation.rotationZ(-angle);
         }

         state.borrowed.put(rotation, Boolean.TRUE);
         return rotation;
      }
   }

   public static void release(Quaternionf rotation) {
      State state = (State)STATE.get();
      if (state.borrowed.remove(rotation) != null && state.available.size() < 64) {
         state.available.offerFirst(rotation);
      }

   }

   public static void clear() {
      State state = (State)STATE.get();
      state.available.clear();
      state.borrowed.clear();
   }

   private static final class State {
      private final ArrayDeque<Quaternionf> available = new ArrayDeque();
      private final Map<Quaternionf, Boolean> borrowed = new IdentityHashMap();
   }
}
