package net.minecraft.client.renderer.debug;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugGoalInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.Vec3;

public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final int MAX_RENDER_DIST = 160;
   private final Minecraft minecraft;

   public GoalSelectorDebugRenderer(final Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void emitGizmos(final double camX, final double camY, final double camZ, final DebugValueAccess debugValues, final Frustum frustum, final float partialTicks) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      BlockPos playerPos = BlockPos.containing(camera.position().x, (double)0.0F, camera.position().z);
      debugValues.forEachEntity(DebugSubscriptions.GOAL_SELECTORS, (entity, goalInfo) -> {
         if (playerPos.closerThan(entity.blockPosition(), (double)160.0F)) {
            for(int i = 0; i < goalInfo.goals().size(); ++i) {
               DebugGoalInfo.DebugGoal goal = (DebugGoalInfo.DebugGoal)goalInfo.goals().get(i);
               double x = (double)entity.getBlockX() + (double)0.5F;
               double y = entity.getY() + (double)2.0F + (double)i * (double)0.25F;
               double z = (double)entity.getBlockZ() + (double)0.5F;
               int color = goal.isRunning() ? -16711936 : -3355444;
               Gizmos.billboardText(goal.name(), new Vec3(x, y, z), TextGizmo.Style.forColorAndCentered(color));
            }
         }

      });
   }
}
