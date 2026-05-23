package net.minecraft.client.renderer.entity.state;

import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EnderDragonRenderState extends EntityRenderState {
   public float flapTime;
   public float deathTime;
   public boolean hasRedOverlay;
   public @Nullable Vec3 beamOffset;
   public boolean isLandingOrTakingOff;
   public boolean isSitting;
   public double distanceToEgg;
   public float partialTicks;
   public final DragonFlightHistory flightHistory = new DragonFlightHistory();

   public DragonFlightHistory.Sample getHistoricalPos(final int delay) {
      return this.flightHistory.get(delay, this.partialTicks);
   }

   public float getHeadPartYOffset(final int part, final DragonFlightHistory.Sample bodyPos, final DragonFlightHistory.Sample partPos) {
      double result;
      if (this.isLandingOrTakingOff) {
         result = (double)part / Math.max(this.distanceToEgg / (double)4.0F, (double)1.0F);
      } else if (this.isSitting) {
         result = (double)part;
      } else if (part == 6) {
         result = (double)0.0F;
      } else {
         result = partPos.y() - bodyPos.y();
      }

      return (float)result;
   }
}
