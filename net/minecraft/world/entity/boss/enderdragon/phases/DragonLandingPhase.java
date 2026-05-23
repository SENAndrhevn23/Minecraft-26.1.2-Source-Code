package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DragonLandingPhase extends AbstractDragonPhaseInstance {
   private @Nullable Vec3 targetLocation;

   public DragonLandingPhase(final EnderDragon dragon) {
      super(dragon);
   }

   public void doClientTick() {
      Vec3 look = this.dragon.getHeadLookVector(1.0F).normalize();
      look.yRot((-(float)Math.PI / 4F));
      double particleX = this.dragon.head.getX();
      double particleY = this.dragon.head.getY((double)0.5F);
      double particleZ = this.dragon.head.getZ();

      for(int i = 0; i < 8; ++i) {
         RandomSource random = this.dragon.getRandom();
         double px = particleX + random.nextGaussian() / (double)2.0F;
         double py = particleY + random.nextGaussian() / (double)2.0F;
         double pz = particleZ + random.nextGaussian() / (double)2.0F;
         Vec3 movement = this.dragon.getDeltaMovement();
         this.dragon.level().addParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F), px, py, pz, -look.x * (double)0.08F + movement.x, -look.y * (double)0.3F + movement.y, -look.z * (double)0.08F + movement.z);
         look.yRot(0.19634955F);
      }

   }

   public void doServerTick(final ServerLevel level) {
      if (this.targetLocation == null) {
         this.targetLocation = Vec3.atBottomCenterOf(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin())));
      }

      if (this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < (double)1.0F) {
         ((DragonSittingFlamingPhase)this.dragon.getPhaseManager().getPhase(EnderDragonPhase.SITTING_FLAMING)).resetFlameCount();
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
      }

   }

   public float getFlySpeed() {
      return 1.5F;
   }

   public float getTurnSpeed() {
      float rotSpeed = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
      float dist = Math.min(rotSpeed, 40.0F);
      return dist / rotSpeed;
   }

   public void begin() {
      this.targetLocation = null;
   }

   public @Nullable Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonLandingPhase> getPhase() {
      return EnderDragonPhase.LANDING;
   }
}
