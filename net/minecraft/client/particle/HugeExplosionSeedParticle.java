package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class HugeExplosionSeedParticle extends NoRenderParticle {
   private HugeExplosionSeedParticle(final ClientLevel level, final double x, final double y, final double z) {
      super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.lifetime = 8;
   }

   public void tick() {
      for(int i = 0; i < 6; ++i) {
         double xx = this.x + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         double yy = this.y + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         double zz = this.z + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         this.level.addParticle(ParticleTypes.EXPLOSION, xx, yy, zz, (double)((float)this.age / (float)this.lifetime), (double)0.0F, (double)0.0F);
      }

      ++this.age;
      if (this.age == this.lifetime) {
         this.remove();
      }

   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z, final double xAux, final double yAux, final double zAux, final RandomSource random) {
         return new HugeExplosionSeedParticle(level, x, y, z);
      }
   }
}
