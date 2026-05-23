package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class EntityShulkerRotationFix extends NamedEntityFix {
   public EntityShulkerRotationFix(final Schema outputSchema) {
      super(outputSchema, false, "EntityShulkerRotationFix", References.ENTITY, "minecraft:shulker");
   }

   public Dynamic<?> fixTag(final Dynamic<?> input) {
      List<Double> rotation = input.get("Rotation").asList((d) -> d.asDouble((double)180.0F));
      if (!rotation.isEmpty()) {
         rotation.set(0, (Double)rotation.get(0) - (double)180.0F);
         Stream var10003 = rotation.stream();
         Objects.requireNonNull(input);
         return input.set("Rotation", input.createList(var10003.map(input::createDouble)));
      } else {
         return input;
      }
   }

   protected Typed<?> fix(final Typed<?> entity) {
      return entity.update(DSL.remainderFinder(), this::fixTag);
   }
}
