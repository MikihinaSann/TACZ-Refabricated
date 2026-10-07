package org.admany.taczt.platform.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;

public final class AttachmentPoseStackPool {
   private static final ThreadLocal<State> LOCAL = ThreadLocal.withInitial(State::new);

   private AttachmentPoseStackPool() {
   }

   public static void enter() {
      ++((State)LOCAL.get()).depth;
   }

   public static PoseStack borrow() {
      State state = (State)LOCAL.get();
      int index = state.depth - 1;
      if (index < 0) {
         throw new IllegalStateException("Attachment pose stack was borrowed outside of its render scope");
      } else {
         if (index == state.stacks.length) {
            PoseStack[] grown = new PoseStack[state.stacks.length << 1];
            System.arraycopy(state.stacks, 0, grown, 0, state.stacks.length);
            state.stacks = grown;
         }

         PoseStack stack = state.stacks[index];
         if (stack == null) {
            stack = new PoseStack();
            state.stacks[index] = stack;
         }

         reset(stack);
         return stack;
      }
   }

   public static void leave() {
      State state = (State)LOCAL.get();
      int index = --state.depth;
      if (index < 0) {
         state.depth = 0;
         throw new IllegalStateException("Attachment pose stack scope underflow");
      } else {
         reset(state.stacks[index]);
      }
   }

   public static void clear() {
      State state = (State)LOCAL.get();
      state.depth = 0;

      for(PoseStack stack : state.stacks) {
         if (stack != null) {
            reset(stack);
         }
      }

   }

   private static void reset(PoseStack stack) {
      while(!stack.clear()) {
         stack.popPose();
      }

      stack.setIdentity();
   }

   private static final class State {
      private PoseStack[] stacks = new PoseStack[4];
      private int depth;
   }
}
