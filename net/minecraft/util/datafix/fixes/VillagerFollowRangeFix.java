package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerFollowRangeFix extends NamedEntityFix {
   private static final double ORIGINAL_VALUE = (double)16.0F;
   private static final double NEW_BASE_VALUE = (double)48.0F;

   public VillagerFollowRangeFix(final Schema outputSchema) {
      super(outputSchema, false, "Villager Follow Range Fix", References.ENTITY, "minecraft:villager");
   }

   protected Typed<?> fix(final Typed<?> entity) {
      return entity.update(DSL.remainderFinder(), VillagerFollowRangeFix::fixValue);
   }

   private static Dynamic<?> fixValue(final Dynamic<?> tag) {
      return tag.update("Attributes", (attributes) -> tag.createList(attributes.asStream().map((attribute) -> attribute.get("Name").asString("").equals("generic.follow_range") && attribute.get("Base").asDouble((double)0.0F) == (double)16.0F ? attribute.set("Base", attribute.createDouble((double)48.0F)) : attribute)));
   }
}
