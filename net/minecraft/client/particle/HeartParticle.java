package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class HeartParticle extends SingleQuadParticle {
   private HeartParticle(final ClientLevel level, final double x, final double y, final double z, final TextureAtlasSprite sprite) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F, sprite);
      this.speedUpWhenYMotionIsBlocked = true;
      this.friction = 0.86F;
      this.xd *= (double)0.01F;
      this.yd *= (double)0.01F;
      this.zd *= (double)0.01F;
      this.yd += 0.1;
      this.quadSize *= 1.5F;
      this.lifetime = 16;
      this.hasPhysics = false;
   }

   public SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.OPAQUE;
   }

   public float getQuadSize(final float a) {
      return this.quadSize * Mth.clamp(((float)this.age + a) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(final SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         HeartParticle particle = new HeartParticle(level, x, y, z, this.sprite.get(random));
         return particle;
      }
   }

   public static class AngryVillagerProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public AngryVillagerProvider(final SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         HeartParticle particle = new HeartParticle(level, x, y + (double)0.5F, z, this.sprite.get(random));
         particle.setColor(1.0F, 1.0F, 1.0F);
         return particle;
      }
   }
}
