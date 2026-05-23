package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public final class LongJumpUtil {
   public static Optional<Vec3> calculateJumpVectorForAngle(final Mob body, final Vec3 targetPos, final float maxJumpVelocity, final int angle, final boolean checkCollision) {
      Vec3 mobPos = body.position();
      Vec3 directionVectorPlane = (new Vec3(targetPos.x - mobPos.x, (double)0.0F, targetPos.z - mobPos.z)).normalize().scale((double)0.5F);
      Vec3 targetPosition = targetPos.subtract(directionVectorPlane);
      Vec3 directionVector = targetPosition.subtract(mobPos);
      float angrad = (float)angle * (float)Math.PI / 180.0F;
      double xzAng = Math.atan2(directionVector.z, directionVector.x);
      double r2 = directionVector.subtract((double)0.0F, directionVector.y, (double)0.0F).lengthSqr();
      double r = Math.sqrt(r2);
      double y = directionVector.y;
      double g = body.getGravity();
      double sin2ang = Math.sin((double)(2.0F * angrad));
      double cosangsqr = Math.pow(Math.cos((double)angrad), (double)2.0F);
      double sinangrad = Math.sin((double)angrad);
      double cosangrad = Math.cos((double)angrad);
      double sinxzAng = Math.sin(xzAng);
      double cosxzAng = Math.cos(xzAng);
      double v0sqr = r2 * g / (r * sin2ang - (double)2.0F * y * cosangsqr);
      if (v0sqr < (double)0.0F) {
         return Optional.empty();
      } else {
         double v0 = Math.sqrt(v0sqr);
         if (v0 > (double)maxJumpVelocity) {
            return Optional.empty();
         } else {
            double v0r = v0 * cosangrad;
            double v0y = v0 * sinangrad;
            if (checkCollision) {
               int samples = Mth.ceil(r / v0r) * 2;
               double ri = (double)0.0F;
               Vec3 previousPos = null;
               EntityDimensions mobDimensions = body.getDimensions(Pose.LONG_JUMPING);

               for(int i = 0; i < samples - 1; ++i) {
                  ri += r / (double)samples;
                  double yi = sinangrad / cosangrad * ri - Math.pow(ri, (double)2.0F) * g / ((double)2.0F * v0sqr * Math.pow(cosangrad, (double)2.0F));
                  double xi = ri * cosxzAng;
                  double zi = ri * sinxzAng;
                  Vec3 samplePos = new Vec3(mobPos.x + xi, mobPos.y + yi, mobPos.z + zi);
                  if (previousPos != null && !isClearTransition(body, mobDimensions, previousPos, samplePos)) {
                     return Optional.empty();
                  }

                  previousPos = samplePos;
               }
            }

            return Optional.of((new Vec3(v0r * cosxzAng, v0y, v0r * sinxzAng)).scale((double)0.95F));
         }
      }
   }

   private static boolean isClearTransition(final Mob body, final EntityDimensions entityDimensions, final Vec3 position1, final Vec3 position2) {
      Vec3 direction = position2.subtract(position1);
      double minDimension = (double)Math.min(entityDimensions.width(), entityDimensions.height());
      int checks = Mth.ceil(direction.length() / minDimension);
      Vec3 normalizedDirection = direction.normalize();
      Vec3 nextPointToCheck = position1;

      for(int i = 0; i < checks; ++i) {
         nextPointToCheck = i == checks - 1 ? position2 : nextPointToCheck.add(normalizedDirection.scale(minDimension * (double)0.9F));
         if (!body.level().noCollision(body, entityDimensions.makeBoundingBox(nextPointToCheck))) {
            return false;
         }
      }

      return true;
   }
}
