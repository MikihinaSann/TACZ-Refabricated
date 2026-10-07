package org.admany.taczt.platform.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayDeque;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class BedrockPoseStackPool {
   private static final int MAX_RETAINED = 96;
   private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

   private BedrockPoseStackPool() {
   }

   public static void enter() {
      State state = (State)STATE.get();
      ++state.activeDepth;
   }

   public static void leave() {
      State state = (State)STATE.get();
      if (state.activeDepth > 0) {
         --state.activeDepth;
      }

   }

   public static boolean isActive() {
      return ((State)STATE.get()).activeDepth > 0;
   }

   public static void clear() {
      State state = (State)STATE.get();
      state.activeDepth = 0;
      state.poseMatrices.clear();
      state.normalMatrices.clear();
   }

   public static Matrix4f borrowPoseMatrix(Matrix4fc source) {
      State state = (State)STATE.get();
      if (state.activeDepth == 0) {
         return new Matrix4f(source);
      } else {
         Matrix4f matrix = (Matrix4f)state.poseMatrices.pollFirst();
         if (matrix == null) {
            matrix = new Matrix4f();
         }

         matrix.set(source);
         return matrix;
      }
   }

   public static Matrix3f borrowNormalMatrix(Matrix3fc source) {
      State state = (State)STATE.get();
      if (state.activeDepth == 0) {
         return new Matrix3f(source);
      } else {
         Matrix3f matrix = (Matrix3f)state.normalMatrices.pollFirst();
         if (matrix == null) {
            matrix = new Matrix3f();
         }

         matrix.set(source);
         return matrix;
      }
   }

   public static void release(PoseStack.Pose pose) {
      State state = (State)STATE.get();
      if (state.activeDepth != 0) {
         if (state.poseMatrices.size() < 96) {
            state.poseMatrices.offerFirst(pose.pose());
         }

         if (state.normalMatrices.size() < 96) {
            state.normalMatrices.offerFirst(pose.normal());
         }

      }
   }

   private static final class State {
      private int activeDepth;
      private final ArrayDeque<Matrix4f> poseMatrices = new ArrayDeque();
      private final ArrayDeque<Matrix3f> normalMatrices = new ArrayDeque();
   }
}
