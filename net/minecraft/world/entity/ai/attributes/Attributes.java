package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class Attributes {
   public static final double DEFAULT_ATTACK_SPEED = (double)4.0F;
   public static final Holder<Attribute> ARMOR = register("armor", (new RangedAttribute("attribute.name.armor", (double)0.0F, (double)0.0F, (double)30.0F)).setSyncable(true));
   public static final Holder<Attribute> ARMOR_TOUGHNESS = register("armor_toughness", (new RangedAttribute("attribute.name.armor_toughness", (double)0.0F, (double)0.0F, (double)20.0F)).setSyncable(true));
   public static final Holder<Attribute> ATTACK_DAMAGE = register("attack_damage", new RangedAttribute("attribute.name.attack_damage", (double)2.0F, (double)0.0F, (double)2048.0F));
   public static final Holder<Attribute> ATTACK_KNOCKBACK = register("attack_knockback", new RangedAttribute("attribute.name.attack_knockback", (double)0.0F, (double)0.0F, (double)5.0F));
   public static final Holder<Attribute> ATTACK_SPEED = register("attack_speed", (new RangedAttribute("attribute.name.attack_speed", (double)4.0F, (double)0.0F, (double)1024.0F)).setSyncable(true));
   public static final Holder<Attribute> BLOCK_BREAK_SPEED = register("block_break_speed", (new RangedAttribute("attribute.name.block_break_speed", (double)1.0F, (double)0.0F, (double)1024.0F)).setSyncable(true));
   public static final Holder<Attribute> BLOCK_INTERACTION_RANGE = register("block_interaction_range", (new RangedAttribute("attribute.name.block_interaction_range", (double)4.5F, (double)0.0F, (double)64.0F)).setSyncable(true));
   public static final Holder<Attribute> BURNING_TIME;
   public static final Holder<Attribute> CAMERA_DISTANCE;
   public static final Holder<Attribute> EXPLOSION_KNOCKBACK_RESISTANCE;
   public static final Holder<Attribute> ENTITY_INTERACTION_RANGE;
   public static final Holder<Attribute> FALL_DAMAGE_MULTIPLIER;
   public static final Holder<Attribute> FLYING_SPEED;
   public static final Holder<Attribute> FOLLOW_RANGE;
   public static final Holder<Attribute> GRAVITY;
   public static final Holder<Attribute> JUMP_STRENGTH;
   public static final Holder<Attribute> KNOCKBACK_RESISTANCE;
   public static final Holder<Attribute> LUCK;
   public static final Holder<Attribute> MAX_ABSORPTION;
   public static final Holder<Attribute> MAX_HEALTH;
   public static final Holder<Attribute> MINING_EFFICIENCY;
   public static final Holder<Attribute> MOVEMENT_EFFICIENCY;
   public static final Holder<Attribute> MOVEMENT_SPEED;
   public static final Holder<Attribute> OXYGEN_BONUS;
   public static final Holder<Attribute> SAFE_FALL_DISTANCE;
   public static final Holder<Attribute> SCALE;
   public static final Holder<Attribute> SNEAKING_SPEED;
   public static final Holder<Attribute> SPAWN_REINFORCEMENTS_CHANCE;
   public static final Holder<Attribute> STEP_HEIGHT;
   public static final Holder<Attribute> SUBMERGED_MINING_SPEED;
   public static final Holder<Attribute> SWEEPING_DAMAGE_RATIO;
   public static final Holder<Attribute> TEMPT_RANGE;
   public static final Holder<Attribute> WATER_MOVEMENT_EFFICIENCY;
   public static final Holder<Attribute> WAYPOINT_TRANSMIT_RANGE;
   public static final Holder<Attribute> WAYPOINT_RECEIVE_RANGE;

   private static Holder<Attribute> register(final String name, final Attribute attribute) {
      return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Identifier.withDefaultNamespace(name), attribute);
   }

   public static Holder<Attribute> bootstrap(final Registry<Attribute> registry) {
      return MAX_HEALTH;
   }

   static {
      BURNING_TIME = register("burning_time", (new RangedAttribute("attribute.name.burning_time", (double)1.0F, (double)0.0F, (double)1024.0F)).setSyncable(true).setSentiment(Attribute.Sentiment.NEGATIVE));
      CAMERA_DISTANCE = register("camera_distance", (new RangedAttribute("attribute.name.camera_distance", (double)4.0F, (double)0.0F, (double)32.0F)).setSyncable(true));
      EXPLOSION_KNOCKBACK_RESISTANCE = register("explosion_knockback_resistance", (new RangedAttribute("attribute.name.explosion_knockback_resistance", (double)0.0F, (double)0.0F, (double)1.0F)).setSyncable(true));
      ENTITY_INTERACTION_RANGE = register("entity_interaction_range", (new RangedAttribute("attribute.name.entity_interaction_range", (double)3.0F, (double)0.0F, (double)64.0F)).setSyncable(true));
      FALL_DAMAGE_MULTIPLIER = register("fall_damage_multiplier", (new RangedAttribute("attribute.name.fall_damage_multiplier", (double)1.0F, (double)0.0F, (double)100.0F)).setSyncable(true).setSentiment(Attribute.Sentiment.NEGATIVE));
      FLYING_SPEED = register("flying_speed", (new RangedAttribute("attribute.name.flying_speed", 0.4, (double)0.0F, (double)1024.0F)).setSyncable(true));
      FOLLOW_RANGE = register("follow_range", new RangedAttribute("attribute.name.follow_range", (double)32.0F, (double)0.0F, (double)2048.0F));
      GRAVITY = register("gravity", (new RangedAttribute("attribute.name.gravity", 0.08, (double)-1.0F, (double)1.0F)).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));
      JUMP_STRENGTH = register("jump_strength", (new RangedAttribute("attribute.name.jump_strength", (double)0.42F, (double)0.0F, (double)32.0F)).setSyncable(true));
      KNOCKBACK_RESISTANCE = register("knockback_resistance", new RangedAttribute("attribute.name.knockback_resistance", (double)0.0F, (double)0.0F, (double)1.0F));
      LUCK = register("luck", (new RangedAttribute("attribute.name.luck", (double)0.0F, (double)-1024.0F, (double)1024.0F)).setSyncable(true));
      MAX_ABSORPTION = register("max_absorption", (new RangedAttribute("attribute.name.max_absorption", (double)0.0F, (double)0.0F, (double)2048.0F)).setSyncable(true));
      MAX_HEALTH = register("max_health", (new RangedAttribute("attribute.name.max_health", (double)20.0F, (double)1.0F, (double)1024.0F)).setSyncable(true));
      MINING_EFFICIENCY = register("mining_efficiency", (new RangedAttribute("attribute.name.mining_efficiency", (double)0.0F, (double)0.0F, (double)1024.0F)).setSyncable(true));
      MOVEMENT_EFFICIENCY = register("movement_efficiency", (new RangedAttribute("attribute.name.movement_efficiency", (double)0.0F, (double)0.0F, (double)1.0F)).setSyncable(true));
      MOVEMENT_SPEED = register("movement_speed", (new RangedAttribute("attribute.name.movement_speed", 0.7, (double)0.0F, (double)1024.0F)).setSyncable(true));
      OXYGEN_BONUS = register("oxygen_bonus", (new RangedAttribute("attribute.name.oxygen_bonus", (double)0.0F, (double)0.0F, (double)1024.0F)).setSyncable(true));
      SAFE_FALL_DISTANCE = register("safe_fall_distance", (new RangedAttribute("attribute.name.safe_fall_distance", (double)3.0F, (double)-1024.0F, (double)1024.0F)).setSyncable(true));
      SCALE = register("scale", (new RangedAttribute("attribute.name.scale", (double)1.0F, (double)0.0625F, (double)16.0F)).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));
      SNEAKING_SPEED = register("sneaking_speed", (new RangedAttribute("attribute.name.sneaking_speed", 0.3, (double)0.0F, (double)1.0F)).setSyncable(true));
      SPAWN_REINFORCEMENTS_CHANCE = register("spawn_reinforcements", new RangedAttribute("attribute.name.spawn_reinforcements", (double)0.0F, (double)0.0F, (double)1.0F));
      STEP_HEIGHT = register("step_height", (new RangedAttribute("attribute.name.step_height", 0.6, (double)0.0F, (double)10.0F)).setSyncable(true));
      SUBMERGED_MINING_SPEED = register("submerged_mining_speed", (new RangedAttribute("attribute.name.submerged_mining_speed", 0.2, (double)0.0F, (double)20.0F)).setSyncable(true));
      SWEEPING_DAMAGE_RATIO = register("sweeping_damage_ratio", (new RangedAttribute("attribute.name.sweeping_damage_ratio", (double)0.0F, (double)0.0F, (double)1.0F)).setSyncable(true));
      TEMPT_RANGE = register("tempt_range", new RangedAttribute("attribute.name.tempt_range", (double)10.0F, (double)0.0F, (double)2048.0F));
      WATER_MOVEMENT_EFFICIENCY = register("water_movement_efficiency", (new RangedAttribute("attribute.name.water_movement_efficiency", (double)0.0F, (double)0.0F, (double)1.0F)).setSyncable(true));
      WAYPOINT_TRANSMIT_RANGE = register("waypoint_transmit_range", (new RangedAttribute("attribute.name.waypoint_transmit_range", (double)0.0F, (double)0.0F, (double)6.0E7F)).setSentiment(Attribute.Sentiment.NEUTRAL));
      WAYPOINT_RECEIVE_RANGE = register("waypoint_receive_range", (new RangedAttribute("attribute.name.waypoint_receive_range", (double)0.0F, (double)0.0F, (double)6.0E7F)).setSentiment(Attribute.Sentiment.NEUTRAL));
   }
}
