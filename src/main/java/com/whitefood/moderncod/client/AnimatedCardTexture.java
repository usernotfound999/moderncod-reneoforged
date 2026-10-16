package com.whitefood.moderncod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Client-only animated texture backed by a list of RGBA frames, used for animated WebP calling
 * cards. Frames are uploaded on demand by {@link #tick()} (driven from the client tick loop), so
 * the card keeps animating wherever it is blitted.
 */
public class AnimatedCardTexture extends AbstractTexture {
   private final NativeImage[] frames;
   private final int[] durations;
   private int index;
   private long nextSwitch;

   public AnimatedCardTexture(NativeImage[] frames, int[] durations) {
      this.frames = frames;
      this.durations = durations;
      this.index = 0;
      this.nextSwitch = System.currentTimeMillis() + durations[0];
      // Allocate the GL texture storage first: without this the texture has no backing store and
      // samples as nothing. DynamicTexture does exactly the same in its constructor.
      TextureUtil.prepareImage(this.getId(), frames[0].getWidth(), frames[0].getHeight());
      this.uploadFrame(0);
   }

   public int frameCount() {
      return this.frames.length;
   }

   @Override
   public void load(ResourceManager resourceManager) {
      // Frames are uploaded manually; there is nothing to load from a resource pack.
   }

   /** Advances to the next frame when its display time has elapsed. */
   public void tick() {
      if (this.frames.length <= 1) {
         return;
      }
      long now = System.currentTimeMillis();
      if (now < this.nextSwitch) {
         return;
      }
      int next = (this.index + 1) % this.frames.length;
      this.uploadFrame(next);
      this.index = next;
      this.nextSwitch = now + this.durations[next];
   }

   private void uploadFrame(int frame) {
      this.bind();
      this.frames[frame].upload(0, 0, 0, false);
   }

   @Override
   public void close() {
      super.close();
      for (NativeImage frame : this.frames) {
         frame.close();
      }
      this.releaseId();
   }
}
