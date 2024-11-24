package dev.nautchkafe.mojang.rest;

import dev.nautchkafe.mojang.rest.AuthUserProfile;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.CompletableFuture;

final class AuthMapper {
    
    private final AsyncLoadingCache<String, AuthUserProfile> cache;

    AuthMapper() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(Duration.ofSeconds(20))
                .buildAsync();
    }

    public CompletableFuture<AuthUserProfile> get(final String username, 
            final CompletableFuture<AuthUserProfile> loader) {
        return cache.get(username, (key, executor) -> loader);
    }
}