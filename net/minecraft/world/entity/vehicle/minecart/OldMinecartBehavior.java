package net.minecraft.world.entity.vehicle.minecart;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class OldMinecartBehavior extends MinecartBehavior {
   private static final double MINECART_RIDABLE_THRESHOLD = 0.01;
   private static final double MAX_SPEED_IN_WATER = 0.2;
   private static final double MAX_SPEED_ON_LAND = 0.4;
   private static final double ABSOLUTE_MAX_SPEED = 0.4;
   private final InterpolationHandler interpolation;
   private Vec3 targetDeltaMovement;

   public OldMinecartBehavior(final AbstractMinecart minecart) {
      super(minecart);
      this.targetDeltaMovement = Vec3.ZERO;
      this.interpolation = new InterpolationHandler(minecart, this::onInterpolation);
   }

   public InterpolationHandler getInterpolation() {
      return this.interpolation;
   }

   public void onInterpolation(final InterpolationHandler interpolation) {
      this.setDeltaMovement(this.targetDeltaMovement);
   }

   public void lerpMotion(final Vec3 movement) {
      this.targetDeltaMovement = movement;
      this.setDeltaMovement(this.targetDeltaMovement);
   }

   public void tick() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel level) {
         this.minecart.applyGravity();
         BlockPos var11 = this.minecart.getCurrentBlockPosOrRailBelow();
         BlockState state = this.level().getBlockState(var11);
         boolean onRails = BaseRailBlock.isRail(state);
         this.minecart.setOnRails(onRails);
         if (onRails) {
            this.moveAlongTrack(level);
            if (state.is(Blocks.ACTIVATOR_RAIL)) {
               this.minecart.activateMinecart(level, var11.getX(), var11.getY(), var11.getZ(), (Boolean)state.getValue(PoweredRailBlock.POWERED));
            }
         } else {
            this.minecart.comeOffTrack(level);
         }

         this.minecart.applyEffectsFromBlocks();
         this.setXRot(0.0F);
         double xDiff = this.minecart.xo - this.getX();
         double zDiff = this.minecart.zo - this.getZ();
         if (xDiff * xDiff + zDiff * zDiff > 0.001) {
            this.setYRot((float)(Mth.atan2(zDiff, xDiff) * (double)180.0F / Math.PI));
            if (this.minecart.isFlipped()) {
               this.setYRot(this.getYRot() + 180.0F);
            }
         }

         double rotDiff = (double)Mth.wrapDegrees(this.getYRot() - this.minecart.yRotO);
         if (rotDiff < (double)-170.0F || rotDiff >= (double)170.0F) {
            this.setYRot(this.getYRot() + 180.0F);
            this.minecart.setFlipped(!this.minecart.isFlipped());
         }

         this.setXRot(this.getXRot() % 360.0F);
         this.setYRot(this.getYRot() % 360.0F);
         this.pushAndPickupEntities();
      } else {
         if (this.interpolation.hasActiveInterpolation()) {
            this.interpolation.interpolate();
         } else {
            this.minecart.reapplyPosition();
            this.setXRot(this.getXRot() % 360.0F);
            this.setYRot(this.getYRot() % 360.0F);
         }

      }
   }

   public void moveAlongTrack(final ServerLevel level) {
      BlockPos pos = this.minecart.getCurrentBlockPosOrRailBelow();
      BlockState state = this.level().getBlockState(pos);
      this.minecart.resetFallDistance();
      double x = this.minecart.getX();
      double y = this.minecart.getY();
      double z = this.minecart.getZ();
      Vec3 oldPos = this.getPos(x, y, z);
      y = (double)pos.getY();
      boolean powerTrack = false;
      boolean haltTrack = false;
      if (state.is(Blocks.POWERED_RAIL)) {
         powerTrack = (Boolean)state.getValue(PoweredRailBlock.POWERED);
         haltTrack = !powerTrack;
      }

      double slideSpeed = (double)0.0078125F;
      if (this.minecart.isInWater()) {
         slideSpeed *= 0.2;
      }

      Vec3 movement = this.getDeltaMovement();
      RailShape shape = (RailShape)state.getValue(((BaseRailBlock)state.getBlock()).getShapeProperty());
      switch (shape) {
         case ASCENDING_EAST:
            this.setDeltaMovement(movement.add(-slideSpeed, (double)0.0F, (double)0.0F));
            ++y;
            break;
         case ASCENDING_WEST:
            this.setDeltaMovement(movement.add(slideSpeed, (double)0.0F, (double)0.0F));
            ++y;
            break;
         case ASCENDING_NORTH:
            this.setDeltaMovement(movement.add((double)0.0F, (double)0.0F, slideSpeed));
            ++y;
            break;
         case ASCENDING_SOUTH:
            this.setDeltaMovement(movement.add((double)0.0F, (double)0.0F, -slideSpeed));
            ++y;
      }

      movement = this.getDeltaMovement();
      Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(shape);
      Vec3i exit0 = (Vec3i)exits.getFirst();
      Vec3i exit1 = (Vec3i)exits.getSecond();
      double xD = (double)(exit1.getX() - exit0.getX());
      double zD = (double)(exit1.getZ() - exit0.getZ());
      double length = Math.sqrt(xD * xD + zD * zD);
      double flip = movement.x * xD + movement.z * zD;
      if (flip < (double)0.0F) {
         xD = -xD;
         zD = -zD;
      }

      double pow = Math.min((double)2.0F, movement.horizontalDistance());
      movement = new Vec3(pow * xD / length, movement.y, pow * zD / length);
      this.setDeltaMovement(movement);
      Entity controllingPassenger = this.minecart.getFirstPassenger();
      Entity var33 = this.minecart.getFirstPassenger();
      Vec3 moveIntent;
      if (var33 instanceof ServerPlayer player) {
         moveIntent = player.getLastClientMoveIntent();
      } else {
         moveIntent = Vec3.ZERO;
      }

      if (controllingPassenger instanceof Player && moveIntent.lengthSqr() > (double)0.0F) {
         Vec3 riderMovement = moveIntent.normalize();
         double ownDist = this.getDeltaMovement().horizontalDistanceSqr();
         if (riderMovement.lengthSqr() > (double)0.0F && ownDist < 0.01) {
            this.setDeltaMovement(this.getDeltaMovement().add(moveIntent.x * 0.001, (double)0.0F, moveIntent.z * 0.001));
            haltTrack = false;
         }
      }

      if (haltTrack) {
         double speedLength = this.getDeltaMovement().horizontalDistance();
         if (speedLength < 0.03) {
            this.setDeltaMovement(Vec3.ZERO);
         } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply((double)0.5F, (double)0.0F, (double)0.5F));
         }
      }

      double x0 = (double)pos.getX() + (double)0.5F + (double)exit0.getX() * (double)0.5F;
      double z0 = (double)pos.getZ() + (double)0.5F + (double)exit0.getZ() * (double)0.5F;
      double x1 = (double)pos.getX() + (double)0.5F + (double)exit1.getX() * (double)0.5F;
      double z1 = (double)pos.getZ() + (double)0.5F + (double)exit1.getZ() * (double)0.5F;
      xD = x1 - x0;
      zD = z1 - z0;
      double progress;
      if (xD == (double)0.0F) {
         progress = z - (double)pos.getZ();
      } else if (zD == (double)0.0F) {
         progress = x - (double)pos.getX();
      } else {
         double xx = x - x0;
         double zz = z - z0;
         progress = (xx * xD + zz * zD) * (double)2.0F;
      }

      x = x0 + xD * progress;
      z = z0 + zD * progress;
      this.setPos(x, y, z);
      double scale = this.minecart.isVehicle() ? (double)0.75F : (double)1.0F;
      double maxSpeed = this.minecart.getMaxSpeed(level);
      movement = this.getDeltaMovement();
      this.minecart.move(MoverType.SELF, new Vec3(Mth.clamp(scale * movement.x, -maxSpeed, maxSpeed), (double)0.0F, Mth.clamp(scale * movement.z, -maxSpeed, maxSpeed)));
      if (exit0.getY() != 0 && Mth.floor(this.minecart.getX()) - pos.getX() == exit0.getX() && Mth.floor(this.minecart.getZ()) - pos.getZ() == exit0.getZ()) {
         this.setPos(this.minecart.getX(), this.minecart.getY() + (double)exit0.getY(), this.minecart.getZ());
      } else if (exit1.getY() != 0 && Mth.floor(this.minecart.getX()) - pos.getX() == exit1.getX() && Mth.floor(this.minecart.getZ()) - pos.getZ() == exit1.getZ()) {
         this.setPos(this.minecart.getX(), this.minecart.getY() + (double)exit1.getY(), this.minecart.getZ());
      }

      this.setDeltaMovement(this.minecart.applyNaturalSlowdown(this.getDeltaMovement()));
      Vec3 newPos = this.getPos(this.minecart.getX(), this.minecart.getY(), this.minecart.getZ());
      if (newPos != null && oldPos != null) {
         double speed = (oldPos.y - newPos.y) * 0.05;
         Vec3 vec3 = this.getDeltaMovement();
         double otherPow = vec3.horizontalDistance();
         if (otherPow > (double)0.0F) {
            this.setDeltaMovement(vec3.multiply((otherPow + speed) / otherPow, (double)1.0F, (otherPow + speed) / otherPow));
         }

         this.setPos(this.minecart.getX(), newPos.y, this.minecart.getZ());
      }

      int xn = Mth.floor(this.minecart.getX());
      int zn = Mth.floor(this.minecart.getZ());
      if (xn != pos.getX() || zn != pos.getZ()) {
         Vec3 vec3 = this.getDeltaMovement();
         double otherPow = vec3.horizontalDistance();
         this.setDeltaMovement(otherPow * (double)(xn - pos.getX()), vec3.y, otherPow * (double)(zn - pos.getZ()));
      }

      if (powerTrack) {
         Vec3 vec3 = this.getDeltaMovement();
         double speedLength = vec3.horizontalDistance();
         if (speedLength > 0.01) {
            double speed = 0.06;
            this.setDeltaMovement(vec3.add(vec3.x / speedLength * 0.06, (double)0.0F, vec3.z / speedLength * 0.06));
         } else {
            Vec3 deltaMovement = this.getDeltaMovement();
            double dx = deltaMovement.x;
            double dz = deltaMovement.z;
            if (shape == RailShape.EAST_WEST) {
               if (this.minecart.isRedstoneConductor(pos.west())) {
                  dx = 0.02;
               } else if (this.minecart.isRedstoneConductor(pos.east())) {
                  dx = -0.02;
               }
            } else {
               if (shape != RailShape.NORTH_SOUTH) {
                  return;
               }

               if (this.minecart.isRedstoneConductor(pos.north())) {
                  dz = 0.02;
               } else if (this.minecart.isRedstoneConductor(pos.south())) {
                  dz = -0.02;
               }
            }

            this.setDeltaMovement(dx, deltaMovement.y, dz);
         }
      }

   }

   public @Nullable Vec3 getPosOffs(double x, double y, double z, final double offs) {
      int xt = Mth.floor(x);
      int yt = Mth.floor(y);
      int zt = Mth.floor(z);
      if (this.level().getBlockState(new BlockPos(xt, yt - 1, zt)).is(BlockTags.RAILS)) {
         --yt;
      }

      BlockState state = this.level().getBlockState(new BlockPos(xt, yt, zt));
      if (BaseRailBlock.isRail(state)) {
         RailShape shape = (RailShape)state.getValue(((BaseRailBlock)state.getBlock()).getShapeProperty());
         y = (double)yt;
         if (shape.isSlope()) {
            y = (double)(yt + 1);
         }

         Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(shape);
         Vec3i exit0 = (Vec3i)exits.getFirst();
         Vec3i exit1 = (Vec3i)exits.getSecond();
         double xD = (double)(exit1.getX() - exit0.getX());
         double zD = (double)(exit1.getZ() - exit0.getZ());
         double dd = Math.sqrt(xD * xD + zD * zD);
         xD /= dd;
         zD /= dd;
         x += xD * offs;
         z += zD * offs;
         if (exit0.getY() != 0 && Mth.floor(x) - xt == exit0.getX() && Mth.floor(z) - zt == exit0.getZ()) {
            y += (double)exit0.getY();
         } else if (exit1.getY() != 0 && Mth.floor(x) - xt == exit1.getX() && Mth.floor(z) - zt == exit1.getZ()) {
            y += (double)exit1.getY();
         }

         return this.getPos(x, y, z);
      } else {
         return null;
      }
   }

   public @Nullable Vec3 getPos(double x, double y, double z) {
      int xt = Mth.floor(x);
      int yt = Mth.floor(y);
      int zt = Mth.floor(z);
      if (this.level().getBlockState(new BlockPos(xt, yt - 1, zt)).is(BlockTags.RAILS)) {
         --yt;
      }

      BlockState state = this.level().getBlockState(new BlockPos(xt, yt, zt));
      if (BaseRailBlock.isRail(state)) {
         RailShape shape = (RailShape)state.getValue(((BaseRailBlock)state.getBlock()).getShapeProperty());
         Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(shape);
         Vec3i exit0 = (Vec3i)exits.getFirst();
         Vec3i exit1 = (Vec3i)exits.getSecond();
         double x0 = (double)xt + (double)0.5F + (double)exit0.getX() * (double)0.5F;
         double y0 = (double)yt + (double)0.0625F + (double)exit0.getY() * (double)0.5F;
         double z0 = (double)zt + (double)0.5F + (double)exit0.getZ() * (double)0.5F;
         double x1 = (double)xt + (double)0.5F + (double)exit1.getX() * (double)0.5F;
         double y1 = (double)yt + (double)0.0625F + (double)exit1.getY() * (double)0.5F;
         double z1 = (double)zt + (double)0.5F + (double)exit1.getZ() * (double)0.5F;
         double xD = x1 - x0;
         double yD = (y1 - y0) * (double)2.0F;
         double zD = z1 - z0;
         double progress;
         if (xD == (double)0.0F) {
            progress = z - (double)zt;
         } else if (zD == (double)0.0F) {
            progress = x - (double)xt;
         } else {
            double xx = x - x0;
            double zz = z - z0;
            progress = (xx * xD + zz * zD) * (double)2.0F;
         }

         x = x0 + xD * progress;
         y = y0 + yD * progress;
         z = z0 + zD * progress;
         if (yD < (double)0.0F) {
            ++y;
         } else if (yD > (double)0.0F) {
            y += (double)0.5F;
         }

         return new Vec3(x, y, z);
      } else {
         return null;
      }
   }

   public double stepAlongTrack(final BlockPos pos, final RailShape shape, final double movementLeft) {
      return (double)0.0F;
   }

   public boolean pushAndPickupEntities() {
      AABB hitbox = this.minecart.getBoundingBox().inflate((double)0.2F, (double)0.0F, (double)0.2F);
      if (this.minecart.isRideable() && this.getDeltaMovement().horizontalDistanceSqr() >= 0.01) {
         List<Entity> entities = this.level().getEntities(this.minecart, hitbox, EntitySelector.pushableBy(this.minecart));
         if (!entities.isEmpty()) {
            for(Entity entity : entities) {
               if (!(entity instanceof Player) && !(entity instanceof IronGolem) && !(entity instanceof AbstractMinecart) && !this.minecart.isVehicle() && !entity.isPassenger()) {
                  entity.startRiding(this.minecart);
               } else {
                  entity.push((Entity)this.minecart);
               }
            }
         }
      } else {
         for(Entity entity : this.level().getEntities(this.minecart, hitbox)) {
            if (!this.minecart.hasPassenger(entity) && entity.isPushable() && entity instanceof AbstractMinecart) {
               entity.push((Entity)this.minecart);
            }
         }
      }

      return false;
   }

   public Direction getMotionDirection() {
      return this.minecart.isFlipped() ? this.minecart.getDirection().getOpposite().getClockWise() : this.minecart.getDirection().getClockWise();
   }

   public Vec3 getKnownMovement(final Vec3 knownMovement) {
      return !Double.isNaN(knownMovement.x) && !Double.isNaN(knownMovement.y) && !Double.isNaN(knownMovement.z) ? new Vec3(Mth.clamp(knownMovement.x, -0.4, 0.4), knownMovement.y, Mth.clamp(knownMovement.z, -0.4, 0.4)) : Vec3.ZERO;
   }

   public double getMaxSpeed(final ServerLevel level) {
      return this.minecart.isInWater() ? 0.2 : 0.4;
   }

   public double getSlowdownFactor() {
      return this.minecart.isVehicle() ? 0.997 : 0.96;
   }
}
