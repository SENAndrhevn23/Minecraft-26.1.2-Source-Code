package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class PlayerCloudParticle extends SingleQuadParticle {
   private final SpriteSet sprites;

   private PlayerCloudParticle(final ClientLevel level, final double x, final double y, final double z, final double xa, final double ya, final double za, final SpriteSet sprites) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F, sprites.first());
      this.friction = 0.96F;
      this.sprites = sprites;
      float scale = 2.5F;
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.xd += xa;
      this.yd += ya;
      this.zd += za;
      float col = 1.0F - this.random.nextFloat() * 0.3F;
      this.rCol = col;
      this.gCol = col;
      this.bCol = col;
      this.quadSize *= 1.875F;
      int baseLifetime = (int)((double)8.0F / ((double)this.random.nextFloat() * 0.8 + 0.3));
      this.lifetime = (int)Math.max((float)baseLifetime * 2.5F, 1.0F);
      this.hasPhysics = false;
      this.setSpriteFromAge(sprites);
   }

   public SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.TRANSLUCENT;
   }

   public float getQuadSize(final float a) {
      return this.quadSize * Mth.clamp(((float)this.age + a) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      if (!this.removed) {
         this.setSpriteFromAge(this.sprites);
         Player player = this.level.getNearestPlayer(this.x, this.y, this.z, (double)2.0F, false);
         if (player != null) {
            double playerY = player.getY();
            if (this.y > playerY) {
               this.y += (playerY - this.y) * 0.2;
               this.yd += (player.getDeltaMovement().y - this.yd) * 0.2;
               this.setPos(this.x, this.y, this.z);
            }
         }
      }

   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(final SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         return new PlayerCloudParticle(level, x, y, z, xAux, yAux, zAux, this.sprites);
      }
   }

   public static class SneezeProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public SneezeProvider(final SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         PlayerCloudParticle particle = new PlayerCloudParticle(level, x, y, z, xAux, yAux, zAux, this.sprites);
         particle.setColor(0.22F, 1.0F, 0.53F);
         particle.setAlpha(0.4F);
         return particle;
      }
   }
}
