package net.minecraft.server.players;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Either;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.StringUtil;

public interface ProfileResolver {
   Optional<GameProfile> fetchByName(String name);

   Optional<GameProfile> fetchById(UUID id);

   default Optional<GameProfile> fetchByNameOrId(final Either<String, UUID> nameOrId) {
      return (Optional)nameOrId.map(this::fetchByName, this::fetchById);
   }

   public static class Cached implements ProfileResolver {
      private final LoadingCache<String, Optional<GameProfile>> profileCacheByName;
      private final LoadingCache<UUID, Optional<GameProfile>> profileCacheById;

      public Cached(final MinecraftSessionService sessionService, final UserNameToIdResolver nameToIdCache) {
         this.profileCacheById = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<UUID, Optional<GameProfile>>() {
            // $FF: synthetic field
            final MinecraftSessionService val$sessionService;

            {
               this.val$sessionService = val$sessionService;
               Objects.requireNonNull(this$0);
               super();
            }

            public Optional<GameProfile> load(final UUID profileId) {
               ProfileResult result = this.val$sessionService.fetchProfile(profileId, true);
               return Optional.ofNullable(result).map(ProfileResult::profile);
            }
         });
         this.profileCacheByName = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<String, Optional<GameProfile>>() {
            // $FF: synthetic field
            final UserNameToIdResolver val$nameToIdCache;
            // $FF: synthetic field
            final ProfileResolver.Cached this$0;

            {
               this.val$nameToIdCache = val$nameToIdCache;
               Objects.requireNonNull(this$0);
               this.this$0 = this$0;
               super();
            }

            public Optional<GameProfile> load(final String name) {
               return this.val$nameToIdCache.get(name).flatMap((nameAndId) -> (Optional)this.this$0.profileCacheById.getUnchecked(nameAndId.id()));
            }

            // $FF: synthetic method
            private Optional lambda$load$0(NameAndId nameAndId) {
               return (Optional)this.this$0.profileCacheById.getUnchecked(nameAndId.id());
            }
         });
      }

      public Optional<GameProfile> fetchByName(final String name) {
         return StringUtil.isValidPlayerName(name) ? (Optional)this.profileCacheByName.getUnchecked(name) : Optional.empty();
      }

      public Optional<GameProfile> fetchById(final UUID id) {
         return (Optional)this.profileCacheById.getUnchecked(id);
      }
   }
}
