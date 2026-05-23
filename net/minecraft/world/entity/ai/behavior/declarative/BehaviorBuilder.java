package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jspecify.annotations.Nullable;

public class BehaviorBuilder<E extends LivingEntity, M> implements App<Mu<E>, M> {
   private final TriggerWithResult<E, M> trigger;

   public static <E extends LivingEntity, M> BehaviorBuilder<E, M> unbox(final App<Mu<E>, M> box) {
      return (BehaviorBuilder)box;
   }

   public static <E extends LivingEntity> Instance<E> instance() {
      return new Instance<E>();
   }

   public static <E extends LivingEntity> OneShot<E> create(final Function<Instance<E>, ? extends App<Mu<E>, Trigger<E>>> builder) {
      final TriggerWithResult<E, Trigger<E>> resolvedBuilder = get((App)builder.apply(instance()));
      return new OneShot<E>() {
         public boolean trigger(final ServerLevel level, final E body, final long timestamp) {
            Trigger<E> trigger = resolvedBuilder.tryTrigger(level, body, timestamp);
            return trigger == null ? false : trigger.trigger(level, body, timestamp);
         }

         public Set<MemoryModuleType<?>> getRequiredMemories() {
            return resolvedBuilder.memories();
         }

         public String debugString() {
            return "OneShot[" + resolvedBuilder.debugString() + "]";
         }

         public String toString() {
            return this.debugString();
         }
      };
   }

   public static <E extends LivingEntity> OneShot<E> sequence(final Trigger<? super E> first, final OneShot<? super E> second) {
      final OneShot<E> wrapped = create((Function)((i) -> i.group(i.ifTriggered(first)).apply(i, (var1) -> {
            Objects.requireNonNull(second);
            return second::trigger;
         })));
      return new OneShot<E>() {
         public boolean trigger(final ServerLevel level, final E body, final long timestamp) {
            return wrapped.trigger(level, body, timestamp);
         }

         public Set<MemoryModuleType<?>> getRequiredMemories() {
            Set<MemoryModuleType<?>> memories = new HashSet();
            memories.addAll(wrapped.getRequiredMemories());
            memories.addAll(second.getRequiredMemories());
            return memories;
         }

         public String debugString() {
            return "OneShot[stuff]";
         }

         public String toString() {
            return this.debugString();
         }
      };
   }

   public static <E extends LivingEntity> OneShot<E> triggerIf(final Predicate<E> predicate, final OneShot<? super E> behavior) {
      return sequence(triggerIf(predicate), behavior);
   }

   public static <E extends LivingEntity> OneShot<E> triggerIf(final Predicate<E> predicate) {
      return create((Function)((i) -> i.point((Trigger)(level, body, timestamp) -> predicate.test(body))));
   }

   public static <E extends LivingEntity> OneShot<E> triggerIf(final BiPredicate<ServerLevel, E> predicate) {
      return create((Function)((i) -> i.point((Trigger)(level, body, timestamp) -> predicate.test(level, body))));
   }

   private static <E extends LivingEntity, M> TriggerWithResult<E, M> get(final App<Mu<E>, M> box) {
      return unbox(box).trigger;
   }

   private BehaviorBuilder(final TriggerWithResult<E, M> trigger) {
      this.trigger = trigger;
   }

   private static <E extends LivingEntity, M> BehaviorBuilder<E, M> create(final TriggerWithResult<E, M> instanceFactory) {
      return new BehaviorBuilder<E, M>(instanceFactory);
   }

   public static final class Mu<E extends LivingEntity> implements K1 {
   }

   private static final class PureMemory<E extends LivingEntity, F extends K1, Value> extends BehaviorBuilder<E, MemoryAccessor<F, Value>> {
      private PureMemory(final MemoryCondition<F, Value> condition) {
         super(new TriggerWithResult<E, MemoryAccessor<F, Value>>() {
            // $FF: synthetic field
            final MemoryCondition val$condition;

            {
               this.val$condition = val$condition;
            }

            public @Nullable MemoryAccessor<F, Value> tryTrigger(final ServerLevel level, final E body, final long timestamp) {
               Brain<?> brain = ((LivingEntity)body).getBrain();
               Optional<Value> value = brain.<Value>getMemoryInternal(this.val$condition.memory());
               return value == null ? null : this.val$condition.createAccessor(brain, value);
            }

            public Set<MemoryModuleType<?>> memories() {
               return Set.of(this.val$condition.memory());
            }

            public String debugString() {
               return "M[" + String.valueOf(this.val$condition) + "]";
            }

            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   private static final class Constant<E extends LivingEntity, A> extends BehaviorBuilder<E, A> {
      private Constant(final A a) {
         this(a, () -> "C[" + String.valueOf(a) + "]");
      }

      private Constant(final A a, final Supplier<String> debugString) {
         super(new TriggerWithResult<E, A>() {
            // $FF: synthetic field
            final Object val$a;
            // $FF: synthetic field
            final Supplier val$debugString;

            {
               this.val$a = val$a;
               this.val$debugString = val$debugString;
            }

            public A tryTrigger(final ServerLevel level, final E body, final long timestamp) {
               return (A)this.val$a;
            }

            public Set<MemoryModuleType<?>> memories() {
               return Set.of();
            }

            public String debugString() {
               return (String)this.val$debugString.get();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   private static final class TriggerWrapper<E extends LivingEntity> extends BehaviorBuilder<E, Unit> {
      private TriggerWrapper(final Trigger<? super E> dependentTrigger) {
         super(new TriggerWithResult<E, Unit>() {
            // $FF: synthetic field
            final Trigger val$dependentTrigger;

            {
               this.val$dependentTrigger = val$dependentTrigger;
            }

            public @Nullable Unit tryTrigger(final ServerLevel level, final E body, final long timestamp) {
               return this.val$dependentTrigger.trigger(level, body, timestamp) ? Unit.INSTANCE : null;
            }

            public Set<MemoryModuleType<?>> memories() {
               return Set.of();
            }

            public String debugString() {
               return "T[" + String.valueOf(this.val$dependentTrigger) + "]";
            }
         });
      }
   }

   public static final class Instance<E extends LivingEntity> implements Applicative<Mu<E>, Mu<E>> {
      public <Value> Optional<Value> tryGet(final MemoryAccessor<OptionalBox.Mu, Value> box) {
         return OptionalBox.unbox(box.value());
      }

      public <Value> Value get(final MemoryAccessor<IdF.Mu, Value> box) {
         return (Value)IdF.get(box.value());
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<OptionalBox.Mu, Value>> registered(final MemoryModuleType<Value> memory) {
         return new PureMemory(new MemoryCondition.Registered(memory));
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<IdF.Mu, Value>> present(final MemoryModuleType<Value> memory) {
         return new PureMemory(new MemoryCondition.Present(memory));
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<Const.Mu<Unit>, Value>> absent(final MemoryModuleType<Value> memory) {
         return new PureMemory(new MemoryCondition.Absent(memory));
      }

      public BehaviorBuilder<E, Unit> ifTriggered(final Trigger<? super E> dependentTrigger) {
         return new TriggerWrapper(dependentTrigger);
      }

      public <A> BehaviorBuilder<E, A> point(final A a) {
         return new Constant<E, A>(a);
      }

      public <A> BehaviorBuilder<E, A> point(final Supplier<String> debugString, final A a) {
         return new Constant<E, A>(a, debugString);
      }

      public <A, R> Function<App<Mu<E>, A>, App<Mu<E>, R>> lift1(final App<Mu<E>, Function<A, R>> function) {
         return (a) -> {
            TriggerWithResult<E, A> aTrigger = BehaviorBuilder.<E, A>get(a);
            TriggerWithResult<E, Function<A, R>> fTrigger = BehaviorBuilder.<E, Function<A, R>>get(function);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>() {
               // $FF: synthetic field
               final BehaviorBuilder.TriggerWithResult val$aTrigger;
               // $FF: synthetic field
               final BehaviorBuilder.TriggerWithResult val$fTrigger;

               {
                  this.val$aTrigger = val$aTrigger;
                  this.val$fTrigger = val$fTrigger;
                  Objects.requireNonNull(this$0);
                  super();
               }

               public R tryTrigger(final ServerLevel level, final E body, final long timestamp) {
                  A ra = (A)this.val$aTrigger.tryTrigger(level, body, timestamp);
                  if (ra == null) {
                     return null;
                  } else {
                     Function<A, R> rf = (Function)this.val$fTrigger.tryTrigger(level, body, timestamp);
                     return (R)(rf == null ? null : rf.apply(ra));
                  }
               }

               public Set<MemoryModuleType<?>> memories() {
                  Set<MemoryModuleType<?>> memories = new HashSet();
                  memories.addAll(this.val$aTrigger.memories());
                  memories.addAll(this.val$fTrigger.memories());
                  return memories;
               }

               public String debugString() {
                  String var10000 = this.val$fTrigger.debugString();
                  return var10000 + " * " + this.val$aTrigger.debugString();
               }

               public String toString() {
                  return this.debugString();
               }
            });
         };
      }

      public <T, R> BehaviorBuilder<E, R> map(final Function<? super T, ? extends R> func, final App<Mu<E>, T> ts) {
         TriggerWithResult<E, T> tTrigger = BehaviorBuilder.<E, T>get(ts);
         return BehaviorBuilder.create(new TriggerWithResult<E, R>() {
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$tTrigger;
            // $FF: synthetic field
            final Function val$func;

            {
               this.val$tTrigger = val$tTrigger;
               this.val$func = val$func;
               Objects.requireNonNull(this$0);
               super();
            }

            public R tryTrigger(final ServerLevel level, final E body, final long timestamp) {
               T t = (T)this.val$tTrigger.tryTrigger(level, body, timestamp);
               return (R)(t == null ? null : this.val$func.apply(t));
            }

            public Set<MemoryModuleType<?>> memories() {
               return this.val$tTrigger.memories();
            }

            public String debugString() {
               String var10000 = this.val$tTrigger.debugString();
               return var10000 + ".map[" + String.valueOf(this.val$func) + "]";
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public <A, B, R> BehaviorBuilder<E, R> ap2(final App<Mu<E>, BiFunction<A, B, R>> func, final App<Mu<E>, A> a, final App<Mu<E>, B> b) {
         TriggerWithResult<E, A> aTrigger = BehaviorBuilder.<E, A>get(a);
         TriggerWithResult<E, B> bTrigger = BehaviorBuilder.<E, B>get(b);
         TriggerWithResult<E, BiFunction<A, B, R>> fTrigger = BehaviorBuilder.<E, BiFunction<A, B, R>>get(func);
         return BehaviorBuilder.create(new TriggerWithResult<E, R>() {
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$aTrigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$bTrigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$fTrigger;

            {
               this.val$aTrigger = val$aTrigger;
               this.val$bTrigger = val$bTrigger;
               this.val$fTrigger = val$fTrigger;
               Objects.requireNonNull(this$0);
               super();
            }

            public R tryTrigger(final ServerLevel level, final E body, final long timestamp) {
               A ra = (A)this.val$aTrigger.tryTrigger(level, body, timestamp);
               if (ra == null) {
                  return null;
               } else {
                  B rb = (B)this.val$bTrigger.tryTrigger(level, body, timestamp);
                  if (rb == null) {
                     return null;
                  } else {
                     BiFunction<A, B, R> fr = (BiFunction)this.val$fTrigger.tryTrigger(level, body, timestamp);
                     return (R)(fr == null ? null : fr.apply(ra, rb));
                  }
               }
            }

            public Set<MemoryModuleType<?>> memories() {
               Set<MemoryModuleType<?>> memories = new HashSet();
               memories.addAll(this.val$aTrigger.memories());
               memories.addAll(this.val$bTrigger.memories());
               memories.addAll(this.val$fTrigger.memories());
               return memories;
            }

            public String debugString() {
               String var10000 = this.val$fTrigger.debugString();
               return var10000 + " * " + this.val$aTrigger.debugString() + " * " + this.val$bTrigger.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public <T1, T2, T3, R> BehaviorBuilder<E, R> ap3(final App<Mu<E>, Function3<T1, T2, T3, R>> func, final App<Mu<E>, T1> t1, final App<Mu<E>, T2> t2, final App<Mu<E>, T3> t3) {
         TriggerWithResult<E, T1> t1Trigger = BehaviorBuilder.<E, T1>get(t1);
         TriggerWithResult<E, T2> t2Trigger = BehaviorBuilder.<E, T2>get(t2);
         TriggerWithResult<E, T3> t3Trigger = BehaviorBuilder.<E, T3>get(t3);
         TriggerWithResult<E, Function3<T1, T2, T3, R>> fTrigger = BehaviorBuilder.<E, Function3<T1, T2, T3, R>>get(func);
         return BehaviorBuilder.create(new TriggerWithResult<E, R>() {
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$t1Trigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$t2Trigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$t3Trigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$fTrigger;

            {
               this.val$t1Trigger = val$t1Trigger;
               this.val$t2Trigger = val$t2Trigger;
               this.val$t3Trigger = val$t3Trigger;
               this.val$fTrigger = val$fTrigger;
               Objects.requireNonNull(this$0);
               super();
            }

            public R tryTrigger(final ServerLevel level, final E body, final long timestamp) {
               T1 r1 = (T1)this.val$t1Trigger.tryTrigger(level, body, timestamp);
               if (r1 == null) {
                  return null;
               } else {
                  T2 r2 = (T2)this.val$t2Trigger.tryTrigger(level, body, timestamp);
                  if (r2 == null) {
                     return null;
                  } else {
                     T3 r3 = (T3)this.val$t3Trigger.tryTrigger(level, body, timestamp);
                     if (r3 == null) {
                        return null;
                     } else {
                        Function3<T1, T2, T3, R> rf = (Function3)this.val$fTrigger.tryTrigger(level, body, timestamp);
                        return (R)(rf == null ? null : rf.apply(r1, r2, r3));
                     }
                  }
               }
            }

            public Set<MemoryModuleType<?>> memories() {
               Set<MemoryModuleType<?>> memories = new HashSet();
               memories.addAll(this.val$t1Trigger.memories());
               memories.addAll(this.val$t2Trigger.memories());
               memories.addAll(this.val$t3Trigger.memories());
               memories.addAll(this.val$fTrigger.memories());
               return memories;
            }

            public String debugString() {
               String var10000 = this.val$fTrigger.debugString();
               return var10000 + " * " + this.val$t1Trigger.debugString() + " * " + this.val$t2Trigger.debugString() + " * " + this.val$t3Trigger.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public <T1, T2, T3, T4, R> BehaviorBuilder<E, R> ap4(final App<Mu<E>, Function4<T1, T2, T3, T4, R>> func, final App<Mu<E>, T1> t1, final App<Mu<E>, T2> t2, final App<Mu<E>, T3> t3, final App<Mu<E>, T4> t4) {
         TriggerWithResult<E, T1> t1Trigger = BehaviorBuilder.<E, T1>get(t1);
         TriggerWithResult<E, T2> t2Trigger = BehaviorBuilder.<E, T2>get(t2);
         TriggerWithResult<E, T3> t3Trigger = BehaviorBuilder.<E, T3>get(t3);
         TriggerWithResult<E, T4> t4Trigger = BehaviorBuilder.<E, T4>get(t4);
         TriggerWithResult<E, Function4<T1, T2, T3, T4, R>> fTrigger = BehaviorBuilder.<E, Function4<T1, T2, T3, T4, R>>get(func);
         return BehaviorBuilder.create(new TriggerWithResult<E, R>() {
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$t1Trigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$t2Trigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$t3Trigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$t4Trigger;
            // $FF: synthetic field
            final BehaviorBuilder.TriggerWithResult val$fTrigger;

            {
               this.val$t1Trigger = val$t1Trigger;
               this.val$t2Trigger = val$t2Trigger;
               this.val$t3Trigger = val$t3Trigger;
               this.val$t4Trigger = val$t4Trigger;
               this.val$fTrigger = val$fTrigger;
               Objects.requireNonNull(this$0);
               super();
            }

            public R tryTrigger(final ServerLevel level, final E body, final long timestamp) {
               T1 r1 = (T1)this.val$t1Trigger.tryTrigger(level, body, timestamp);
               if (r1 == null) {
                  return null;
               } else {
                  T2 r2 = (T2)this.val$t2Trigger.tryTrigger(level, body, timestamp);
                  if (r2 == null) {
                     return null;
                  } else {
                     T3 r3 = (T3)this.val$t3Trigger.tryTrigger(level, body, timestamp);
                     if (r3 == null) {
                        return null;
                     } else {
                        T4 r4 = (T4)this.val$t4Trigger.tryTrigger(level, body, timestamp);
                        if (r4 == null) {
                           return null;
                        } else {
                           Function4<T1, T2, T3, T4, R> rf = (Function4)this.val$fTrigger.tryTrigger(level, body, timestamp);
                           return (R)(rf == null ? null : rf.apply(r1, r2, r3, r4));
                        }
                     }
                  }
               }
            }

            public Set<MemoryModuleType<?>> memories() {
               Set<MemoryModuleType<?>> memories = new HashSet();
               memories.addAll(this.val$t1Trigger.memories());
               memories.addAll(this.val$t2Trigger.memories());
               memories.addAll(this.val$t3Trigger.memories());
               memories.addAll(this.val$t4Trigger.memories());
               memories.addAll(this.val$fTrigger.memories());
               return memories;
            }

            public String debugString() {
               String var10000 = this.val$fTrigger.debugString();
               return var10000 + " * " + this.val$t1Trigger.debugString() + " * " + this.val$t2Trigger.debugString() + " * " + this.val$t3Trigger.debugString() + " * " + this.val$t4Trigger.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      private static final class Mu<E extends LivingEntity> implements Applicative.Mu {
      }
   }

   private interface TriggerWithResult<E extends LivingEntity, R> {
      @Nullable R tryTrigger(final ServerLevel level, final E body, final long timestamp);

      Set<MemoryModuleType<?>> memories();

      String debugString();
   }
}
