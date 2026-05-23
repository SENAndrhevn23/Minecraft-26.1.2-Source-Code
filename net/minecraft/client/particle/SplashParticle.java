package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class SplashParticle extends WaterDropParticle {
   private SplashParticle(final ClientLevel level, final double x, final double y, final double z, final double xa, final double ya, final double za, final TextureAtlasSprite sprite) {
      super(level, x, y, z, sprite);
      this.gravity = 0.04F;
      if (ya == (double)0.0F && (xa != (double)0.0F || za != (double)0.0F)) {
         this.xd = xa;
         this.yd = 0.1;
         this.zd = za;
      }

   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(final SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         return new SplashParticle(level, x, y, z, xAux, yAux, zAux, this.sprite.get(random));
      }
   }
}
