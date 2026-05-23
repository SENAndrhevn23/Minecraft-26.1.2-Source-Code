package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DragonLandingApproachPhase extends AbstractDragonPhaseInstance {
   private static final TargetingConditions NEAR_EGG_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
   private @Nullable Path currentPath;
   private @Nullable Vec3 targetLocation;

   public DragonLandingApproachPhase(final EnderDragon dragon) {
      super(dragon);
   }

   public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
      return EnderDragonPhase.LANDING_APPROACH;
   }

   public void begin() {
      this.currentPath = null;
      this.targetLocation = null;
   }

   public void doServerTick(final ServerLevel level) {
      double distToTarget = this.targetLocation == null ? (double)0.0F : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (distToTarget < (double)100.0F || distToTarget > (double)22500.0F || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.findNewTarget(level);
      }

   }

   public @Nullable Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   private void findNewTarget(final ServerLevel level) {
      if (this.currentPath == null || this.currentPath.isDone()) {
         int currentNodeIndex = this.dragon.findClosestNode();
         BlockPos egg = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()));
         Player playerNearestToEgg = level.getNearestPlayer(NEAR_EGG_TARGETING, this.dragon, (double)egg.getX(), (double)egg.getY(), (double)egg.getZ());
         int targetNodeIndex;
         if (playerNearestToEgg != null) {
            Vec3 aim = (new Vec3(playerNearestToEgg.getX(), (double)0.0F, playerNearestToEgg.getZ())).normalize();
            targetNodeIndex = this.dragon.findClosestNode(-aim.x * (double)40.0F, (double)105.0F, -aim.z * (double)40.0F);
         } else {
            targetNodeIndex = this.dragon.findClosestNode((double)40.0F, (double)egg.getY(), (double)0.0F);
         }

         Node finalNode = new Node(egg.getX(), egg.getY(), egg.getZ());
         this.currentPath = this.dragon.findPath(currentNodeIndex, targetNodeIndex, finalNode);
         if (this.currentPath != null) {
            this.currentPath.advance();
         }
      }

      this.navigateToNextPathNode();
      if (this.currentPath != null && this.currentPath.isDone()) {
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
      }

   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.isDone()) {
         Vec3i current = this.currentPath.getNextNodePos();
         this.currentPath.advance();
         double xTarget = (double)current.getX();
         double zTarget = (double)current.getZ();

         double yTarget;
         do {
            yTarget = (double)((float)current.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(yTarget < (double)current.getY());

         this.targetLocation = new Vec3(xTarget, yTarget, zTarget);
      }

   }
}
