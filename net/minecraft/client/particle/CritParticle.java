package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class CritParticle extends SingleQuadParticle {
   private CritParticle(final ClientLevel level, final double x, final double y, final double z, final double xa, final double ya, final double za, final TextureAtlasSprite sprite) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F, sprite);
      this.friction = 0.7F;
      this.gravity = 0.5F;
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.xd += xa * 0.4;
      this.yd += ya * 0.4;
      this.zd += za * 0.4;
      float col = this.random.nextFloat() * 0.3F + 0.6F;
      this.rCol = col;
      this.gCol = col;
      this.bCol = col;
      this.quadSize *= 0.75F;
      this.lifetime = Math.max((int)((double)6.0F / ((double)this.random.nextFloat() * 0.8 + 0.6)), 1);
      this.hasPhysics = false;
      this.tick();
   }

   public float getQuadSize(final float a) {
      return this.quadSize * Mth.clamp(((float)this.age + a) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      this.gCol *= 0.96F;
      this.bCol *= 0.9F;
   }

   public SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.OPAQUE;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(final SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         CritParticle particle = new CritParticle(level, x, y, z, xAux, yAux, zAux, this.sprite.get(random));
         return particle;
      }
   }

   public static class MagicProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public MagicProvider(final SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         CritParticle particle = new CritParticle(level, x, y, z, xAux, yAux, zAux, this.sprite.get(random));
         particle.rCol *= 0.3F;
         particle.gCol *= 0.8F;
         return particle;
      }
   }

   public static class DamageIndicatorProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public DamageIndicatorProvider(final SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         CritParticle particle = new CritParticle(level, x, y, z, xAux, yAux + (double)1.0F, zAux, this.sprite.get(random));
         particle.setLifetime(20);
         return particle;
      }
   }
}
