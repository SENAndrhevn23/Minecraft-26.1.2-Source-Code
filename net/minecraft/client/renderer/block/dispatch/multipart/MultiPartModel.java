package net.minecraft.client.renderer.block.dispatch.multipart;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MultiPartModel implements BlockStateModel {
   private final SharedBakedState shared;
   private final BlockState blockState;
   private @Nullable List<BlockStateModel> models;

   private MultiPartModel(final SharedBakedState shared, final BlockState blockState) {
      this.shared = shared;
      this.blockState = blockState;
   }

   public Material.Baked particleMaterial() {
      return this.shared.particleMaterial;
   }

   public @BakedQuad.MaterialFlags int materialFlags() {
      return this.shared.materialFlags;
   }

   public void collectParts(final RandomSource random, final List<BlockStateModelPart> output) {
      if (this.models == null) {
         this.models = this.shared.selectModels(this.blockState);
      }

      long seed = random.nextLong();

      for(BlockStateModel model : this.models) {
         random.setSeed(seed);
         model.collectParts(random, output);
      }

   }

   public static record Selector<T>(Predicate<BlockState> condition, T model) {
      public <S> Selector<S> with(final S newModel) {
         return new Selector<S>(this.condition, newModel);
      }
   }

   private static final class SharedBakedState {
      private final List<Selector<BlockStateModel>> selectors;
      private final Material.Baked particleMaterial;
      private final @BakedQuad.MaterialFlags int materialFlags;
      private final Map<BitSet, List<BlockStateModel>> subsets = new ConcurrentHashMap();

      private static BlockStateModel getFirstModel(final List<Selector<BlockStateModel>> selectors) {
         if (selectors.isEmpty()) {
            throw new IllegalArgumentException("Model must have at least one selector");
         } else {
            return (BlockStateModel)((Selector)selectors.getFirst()).model();
         }
      }

      private static @BakedQuad.MaterialFlags int computeMaterialFlags(final List<Selector<BlockStateModel>> selectors) {
         int flags = 0;

         for(Selector<BlockStateModel> selector : selectors) {
            flags |= ((BlockStateModel)selector.model).materialFlags();
         }

         return flags;
      }

      public SharedBakedState(final List<Selector<BlockStateModel>> selectors) {
         this.selectors = selectors;
         BlockStateModel firstModel = getFirstModel(selectors);
         this.particleMaterial = firstModel.particleMaterial();
         this.materialFlags = computeMaterialFlags(selectors);
      }

      public List<BlockStateModel> selectModels(final BlockState state) {
         BitSet selectedModels = new BitSet();

         for(int i = 0; i < this.selectors.size(); ++i) {
            if (((Selector)this.selectors.get(i)).condition.test(state)) {
               selectedModels.set(i);
            }
         }

         return (List)this.subsets.computeIfAbsent(selectedModels, (selected) -> {
            ImmutableList.Builder<BlockStateModel> result = ImmutableList.builder();

            for(int i = 0; i < this.selectors.size(); ++i) {
               if (selected.get(i)) {
                  result.add(((Selector)this.selectors.get(i)).model);
               }
            }

            return result.build();
         });
      }
   }

   public static class Unbaked implements BlockStateModel.UnbakedRoot {
      private final List<Selector<BlockStateModel.Unbaked>> selectors;
      private final ModelBaker.SharedOperationKey<SharedBakedState> sharedStateKey = new ModelBaker.SharedOperationKey<SharedBakedState>() {
         // $FF: synthetic field
         final MultiPartModel.Unbaked this$0;

         {
            Objects.requireNonNull(this$0);
            this.this$0 = this$0;
            super();
         }

         public MultiPartModel.SharedBakedState compute(final ModelBaker modelBakery) {
            ImmutableList.Builder<MultiPartModel.Selector<BlockStateModel>> selectors = ImmutableList.builderWithExpectedSize(this.this$0.selectors.size());

            for(MultiPartModel.Selector<BlockStateModel.Unbaked> selector : this.this$0.selectors) {
               selectors.add(selector.with(((BlockStateModel.Unbaked)selector.model).bake(modelBakery)));
            }

            return new MultiPartModel.SharedBakedState(selectors.build());
         }
      };

      public Unbaked(final List<Selector<BlockStateModel.Unbaked>> selectors) {
         this.selectors = selectors;
      }

      public Object visualEqualityGroup(final BlockState blockState) {
         IntList triggeredSelectors = new IntArrayList();

         for(int i = 0; i < this.selectors.size(); ++i) {
            if (((Selector)this.selectors.get(i)).condition.test(blockState)) {
               triggeredSelectors.add(i);
            }
         }

         return new Key(this, triggeredSelectors);
      }

      public void resolveDependencies(final ResolvableModel.Resolver resolver) {
         this.selectors.forEach((s) -> ((BlockStateModel.Unbaked)s.model).resolveDependencies(resolver));
      }

      public BlockStateModel bake(final BlockState blockState, final ModelBaker modelBakery) {
         SharedBakedState shared = (SharedBakedState)modelBakery.compute(this.sharedStateKey);
         return new MultiPartModel(shared, blockState);
      }
   }
}
