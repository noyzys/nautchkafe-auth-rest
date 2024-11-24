package dev.nautchkafe.mojang.rest;

import dev.nautchkafe.mojang.rest.AuthMapper;
import dev.nautchkafe.mojang.rest.AuthUserProfile;
import dev.nautchkafe.mojang.rest.AuthCoordinator;
import io.vavr.control.Either;

import java.util.concurrent.CompletableFuture;

final class AuthService {

    private final AuthCoordinator authCoordinator;
    private final AuthMapper authCache;

    AuthService(final AuthCoordinator authCoordinator, final AuthMapper authCache) {
        this.authCoordinator = authCoordinator;
        this.authCache = authCache;
    }

    public CompletableFuture<Either<String, AuthUserProfile>> authenticate(final String username) {
        return authCache.get(username, () -> authCoordinator.authenticate(username))
                .thenApply(Either::right)
                .exceptionally(t -> Either.left("> Error fetching player data: " + t.getMessage()));
    }

    public CompletableFuture<Boolean> isPremium(final String username) {
        return authCache.get(username, authCoordinator.authenticate(username)
            .thenApply(Either::get)).thenApply(AuthUserProfile::premium);
    }
}