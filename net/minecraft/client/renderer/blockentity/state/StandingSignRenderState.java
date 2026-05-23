package net.minecraft.client.renderer.blockentity.state;

import net.minecraft.world.level.block.PlainSignBlock;

public class StandingSignRenderState extends SignRenderState {
   public PlainSignBlock.Attachment attachmentType;

   public StandingSignRenderState() {
      this.attachmentType = PlainSignBlock.Attachment.GROUND;
   }
}
