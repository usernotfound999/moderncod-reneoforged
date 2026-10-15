package com.whitefood.moderncod.client;

import com.whitefood.moderncod.callingcard.CallingCard;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.C2SRequestCallingCardPacket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side store mapping each player UUID to the texture of their currently equipped calling
 * card. Cards are resolved to a {@link ResourceLocation} up-front, so rendering code can always
 * fall back to {@link CallingCard#DEFAULT_IMAGE} and never has to blit a missing texture.
 */
public class ClientImageDataManager {
   private static final int[] DEFAULT_SIZE = {128, 32};

   private static final Map<UUID, ResourceLocation> playerTextures = new HashMap<>();
   private static final Map<UUID, String> pendingHashes = new HashMap<>();
   private static final Set<String> requestedHashes = new HashSet<>();

   public ClientImageDataManager() {
   }

   /** Equips a built-in resource for the player. */
   public static void setBuiltin(UUID uuid, String path) {
      String resolved = path != null && !path.isEmpty() ? path : CallingCard.DEFAULT_IMAGE.getPath();
      playerTextures.put(uuid, CallingCard.builtinTexture(resolved));
      pendingHashes.remove(uuid);
   }

   /** Equips a custom image; requests the bytes from the server when not cached locally yet. */
   public static void setCustom(UUID uuid, String hash) {
      if (!CallingCard.isCustom(hash)) {
         setBuiltin(uuid, CallingCard.DEFAULT_IMAGE.getPath());
         return;
      }
      ResourceLocation location = CallingCardTextures.location(hash);
      if (location != null) {
         playerTextures.put(uuid, location);
         pendingHashes.remove(uuid);
      } else {
         pendingHashes.put(uuid, hash);
         request(hash);
      }
   }

   /** @return the texture to render for the player, never {@code null}. */
   public static ResourceLocation getTexture(UUID uuid) {
      return playerTextures.getOrDefault(uuid, CallingCard.DEFAULT_IMAGE);
   }

   /** @return the source dimensions of the player's card, for scaling into the nameplate box. */
   public static int[] getTextureSize(UUID uuid) {
      ResourceLocation location = playerTextures.get(uuid);
      if (location != null && CallingCard.NAMESPACE.equals(location.getNamespace())
         && location.getPath().startsWith("dynamic/callingcard/")) {
         String hash = location.getPath().substring("dynamic/callingcard/".length());
         int[] size = CallingCardTextures.size(hash);
         if (size != null) {
            return size;
         }
      }
      return DEFAULT_SIZE;
   }

   /** Called once a custom texture has been loaded, to resolve every player waiting on that hash. */
   public static void onTextureLoaded(String hash) {
      ResourceLocation location = CallingCardTextures.location(hash);
      if (location == null) {
         return;
      }
      pendingHashes.entrySet().removeIf(entry -> {
         if (entry.getValue().equals(hash)) {
            playerTextures.put(entry.getKey(), location);
            return true;
         }
         return false;
      });
      requestedHashes.remove(hash);
   }

   public static void clear() {
      playerTextures.clear();
      pendingHashes.clear();
      requestedHashes.clear();
   }

   private static void request(String hash) {
      if (requestedHashes.add(hash)) {
         ModMessages.sendToServer(new C2SRequestCallingCardPacket(hash));
      }
   }
}
