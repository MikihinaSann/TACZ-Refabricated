package org.admany.taczt.platform.fabric.client;

import java.util.ArrayDeque;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class FunctionalMatrixPool {
   private static final ThreadLocal<State> LOCAL = ThreadLocal.withInitial(State::new);

   private FunctionalMatrixPool() {
   }

   public static Matrix3f borrow(Matrix3fc source) {
      State state = (State)LOCAL.get();
      Matrix3f matrix = (Matrix3f)state.matrices3.pollFirst();
      if (matrix == null) {
         matrix = new Matrix3f();
      }

      return matrix.set(source);
   }

   public static Matrix4f borrow(Matrix4fc source) {
      State state = (State)LOCAL.get();
      Matrix4f matrix = (Matrix4f)state.matrices4.pollFirst();
      if (matrix == null) {
         matrix = new Matrix4f();
      }

      return matrix.set(source);
   }

   public static void release(Matrix3f matrix) {
      ((State)LOCAL.get()).matrices3.offerFirst(matrix);
   }

   public static void release(Matrix4f matrix) {
      ((State)LOCAL.get()).matrices4.offerFirst(matrix);
   }

   private static final class State {
      private final ArrayDeque<Matrix3f> matrices3 = new ArrayDeque();
      private final ArrayDeque<Matrix4f> matrices4 = new ArrayDeque();
   }
}
