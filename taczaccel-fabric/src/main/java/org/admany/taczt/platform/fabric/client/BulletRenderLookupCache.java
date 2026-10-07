package org.admany.taczt.platform.fabric.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class BulletRenderLookupCache {
   private static final int MAX_DISPLAY_ENTRIES = 512;
   private static final int MAX_AMMO_ENTRIES = 512;
   private static final int MAX_ATTACHMENT_ENTRIES = 512;
   private static final Map<ResourceLocation, Map<ResourceLocation, Optional<GunDisplayInstance>>> DISPLAY_CACHE = new HashMap();
   private static final Map<ResourceLocation, Optional<ClientAmmoIndex>> AMMO_CACHE = new HashMap();
   private static final Map<ResourceLocation, Optional<ClientAttachmentIndex>> ATTACHMENT_CACHE = new HashMap();
   private static int displayEntries;

   private BulletRenderLookupCache() {
   }

   public static Optional<GunDisplayInstance> getGunDisplay(ResourceLocation displayId, ResourceLocation gunId) {
      if (RenderReloadLifecycle.isReloading()) {
         return TimelessAPI.getGunDisplay(displayId, gunId);
      } else {
         Map<ResourceLocation, Optional<GunDisplayInstance>> byGun = (Map)DISPLAY_CACHE.get(displayId);
         if (byGun != null) {
            Optional<GunDisplayInstance> cached = (Optional)byGun.get(gunId);
            if (cached != null) {
               return cached;
            }
         }

         Optional<GunDisplayInstance> resolved = TimelessAPI.getGunDisplay(displayId, gunId);
         if (displayEntries < 512) {
            if (byGun == null) {
               byGun = new HashMap();
               DISPLAY_CACHE.put(displayId, byGun);
            }

            if (!byGun.containsKey(gunId)) {
               byGun.put(gunId, resolved);
               ++displayEntries;
            }
         }

         return resolved;
      }
   }

   public static Optional<GunDisplayInstance> getGunDisplay(ItemStack stack) {
      IGun gun = IGun.getIGunOrNull(stack);
      if (gun == null) {
         return TimelessAPI.getGunDisplay(stack);
      } else {
         ResourceLocation displayId = gun.getGunDisplayId(stack);
         ResourceLocation gunId = gun.getGunId(stack);
         return displayId != null && gunId != null ? getGunDisplay(displayId, gunId) : TimelessAPI.getGunDisplay(stack);
      }
   }

   public static Optional<ClientAmmoIndex> getAmmoIndex(ResourceLocation ammoId) {
      if (RenderReloadLifecycle.isReloading()) {
         return TimelessAPI.getClientAmmoIndex(ammoId);
      } else {
         Optional<ClientAmmoIndex> cached = (Optional)AMMO_CACHE.get(ammoId);
         if (cached != null) {
            return cached;
         } else {
            Optional<ClientAmmoIndex> resolved = TimelessAPI.getClientAmmoIndex(ammoId);
            if (AMMO_CACHE.size() < 512) {
               AMMO_CACHE.put(ammoId, resolved);
            }

            return resolved;
         }
      }
   }

   public static Optional<ClientAttachmentIndex> getAttachmentIndex(ResourceLocation attachmentId) {
      if (RenderReloadLifecycle.isReloading()) {
         return TimelessAPI.getClientAttachmentIndex(attachmentId);
      } else {
         Optional<ClientAttachmentIndex> cached = (Optional)ATTACHMENT_CACHE.get(attachmentId);
         if (cached != null) {
            return cached;
         } else {
            Optional<ClientAttachmentIndex> resolved = TimelessAPI.getClientAttachmentIndex(attachmentId);
            if (ATTACHMENT_CACHE.size() < 512) {
               ATTACHMENT_CACHE.put(attachmentId, resolved);
            }

            return resolved;
         }
      }
   }

   public static void clear() {
      DISPLAY_CACHE.clear();
      AMMO_CACHE.clear();
      ATTACHMENT_CACHE.clear();
      displayEntries = 0;
   }
}
