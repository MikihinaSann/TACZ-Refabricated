package org.admany.taczt.platform.fabric.client;

import com.tacz.guns.client.model.bedrock.BedrockModel;

public final class BulletModelCache {
   private static volatile BedrockModel defaultBulletModel;

   private BulletModelCache() {
   }

   public static BedrockModel get() {
      return defaultBulletModel;
   }

   public static void set(BedrockModel model) {
      defaultBulletModel = model;
   }

   public static void clear() {
      defaultBulletModel = null;
   }
}
