package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MaceItem extends Item {
   private static final int DEFAULT_ATTACK_DAMAGE = 5;
   private static final float DEFAULT_ATTACK_SPEED = -3.4F;
   public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
   private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
   public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5F;
   private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7F;

   public MaceItem(final Item.Properties properties) {
      super(properties);
   }

   public static ItemAttributeModifiers createAttributes() {
      return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)5.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)-3.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
   }

   public static Tool createToolProperties() {
      return new Tool(List.of(), 1.0F, 2, false);
   }

   public void hurtEnemy(final ItemStack itemStack, final LivingEntity mob, final LivingEntity attacker) {
      if (canSmashAttack(attacker)) {
         ServerLevel level = (ServerLevel)attacker.level();
         attacker.setDeltaMovement(attacker.getDeltaMovement().with(Direction.Axis.Y, (double)0.01F));
         attacker.setIgnoreFallDamageFromCurrentImpulse(true, this.calculateImpactPosition(attacker));
         if (attacker instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)attacker;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
         }

         if (mob.onGround()) {
            if (attacker instanceof ServerPlayer) {
               ServerPlayer player = (ServerPlayer)attacker;
               player.setSpawnExtraParticlesOnFall(true);
            }

            SoundEvent sound = attacker.fallDistance > (double)5.0F ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
            level.playSound((Entity)null, attacker.getX(), attacker.getY(), attacker.getZ(), sound, attacker.getSoundSource(), 1.0F, 1.0F);
         } else {
            level.playSound((Entity)null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.MACE_SMASH_AIR, attacker.getSoundSource(), 1.0F, 1.0F);
         }

         knockback(level, attacker, mob);
      }

   }

   private Vec3 calculateImpactPosition(final LivingEntity attacker) {
      return attacker.isIgnoringFallDamageFromCurrentImpulse() && attacker.currentImpulseImpactPos.y <= attacker.position().y ? attacker.currentImpulseImpactPos : attacker.position();
   }

   public void postHurtEnemy(final ItemStack itemStack, final LivingEntity mob, final LivingEntity attacker) {
      if (canSmashAttack(attacker)) {
         attacker.resetFallDistance();
      }

   }

   public float getAttackDamageBonus(final Entity victim, final float ignoredDamage, final DamageSource damageSource) {
      Entity var5 = damageSource.getDirectEntity();
      if (var5 instanceof LivingEntity attacker) {
         if (!canSmashAttack(attacker)) {
            return 0.0F;
         } else {
            double fallHeightThreshold1 = (double)3.0F;
            double fallHeightThreshold2 = (double)8.0F;
            double fallDistance = attacker.fallDistance;
            double damage;
            if (fallDistance <= (double)3.0F) {
               damage = (double)4.0F * fallDistance;
            } else if (fallDistance <= (double)8.0F) {
               damage = (double)12.0F + (double)2.0F * (fallDistance - (double)3.0F);
            } else {
               damage = (double)22.0F + fallDistance - (double)8.0F;
            }

            Level var14 = attacker.level();
            if (var14 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var14;
               return (float)(damage + (double)EnchantmentHelper.modifyFallBasedDamage(level, attacker.getWeaponItem(), victim, damageSource, 0.0F) * fallDistance);
            } else {
               return (float)damage;
            }
         }
      } else {
         return 0.0F;
      }
   }

   private static void knockback(final Level level, final Entity attacker, final Entity entity) {
      level.levelEvent(2013, entity.getOnPos(), 750);
      level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate((double)3.5F), knockbackPredicate(attacker, entity)).forEach((nearby) -> {
         Vec3 direction = nearby.position().subtract(entity.position());
         double knockbackPower = getKnockbackPower(attacker, nearby, direction);
         Vec3 knockbackVector = direction.normalize().scale(knockbackPower);
         if (knockbackPower > (double)0.0F) {
            nearby.push(knockbackVector.x, (double)0.7F, knockbackVector.z);
            if (nearby instanceof ServerPlayer) {
               ServerPlayer otherPlayer = (ServerPlayer)nearby;
               otherPlayer.connection.send(new ClientboundSetEntityMotionPacket(otherPlayer));
            }
         }

      });
   }

   private static Predicate<LivingEntity> knockbackPredicate(final Entity attacker, final Entity entity) {
      return (nearby) -> {
         boolean notSpectator;
         boolean notPlayer;
         boolean notAlliedToPlayer;
         boolean var10000;
         label82: {
            notSpectator = !nearby.isSpectator();
            notPlayer = nearby != attacker && nearby != entity;
            notAlliedToPlayer = !attacker.isAlliedTo((Entity)nearby);
            if (nearby instanceof TamableAnimal animal) {
               if (entity instanceof LivingEntity livingAttacker) {
                  if (animal.isTame() && animal.isOwnedBy(livingAttacker)) {
                     var10000 = true;
                     break label82;
                  }
               }
            }

            var10000 = false;
         }

         boolean notTamedByPlayer;
         label74: {
            notTamedByPlayer = !var10000;
            if (nearby instanceof ArmorStand armorStand) {
               if (armorStand.isMarker()) {
                  var10000 = false;
                  break label74;
               }
            }

            var10000 = true;
         }

         boolean notArmorStand;
         boolean withinRange;
         label68: {
            notArmorStand = var10000;
            withinRange = entity.distanceToSqr((Entity)nearby) <= Math.pow((double)3.5F, (double)2.0F);
            if (nearby instanceof Player player) {
               if (player.isCreative() && player.getAbilities().flying) {
                  var10000 = true;
                  break label68;
               }
            }

            var10000 = false;
         }

         boolean notFlyingInCreative = !var10000;
         return notSpectator && notPlayer && notAlliedToPlayer && notTamedByPlayer && notArmorStand && withinRange && notFlyingInCreative;
      };
   }

   private static double getKnockbackPower(final Entity attacker, final LivingEntity nearby, final Vec3 direction) {
      return ((double)3.5F - direction.length()) * (double)0.7F * (double)(attacker.fallDistance > (double)5.0F ? 2 : 1) * ((double)1.0F - nearby.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
   }

   public static boolean canSmashAttack(final LivingEntity attacker) {
      return attacker.fallDistance > (double)1.5F && !attacker.isFallFlying();
   }

   public @Nullable DamageSource getItemDamageSource(final LivingEntity attacker) {
      return canSmashAttack(attacker) ? attacker.damageSources().mace(attacker) : super.getItemDamageSource(attacker);
   }
}
